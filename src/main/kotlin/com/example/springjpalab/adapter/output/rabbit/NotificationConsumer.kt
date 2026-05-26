package com.example.springjpalab.adapter.output.rabbit

import com.example.springjpalab.adapter.output.jpa.entity.ProcessedEventJpaEntity
import com.example.springjpalab.adapter.output.jpa.repository.ProcessedEventJpaRepository
import com.example.springjpalab.application.service.NotificationService
import com.example.springjpalab.config.RabbitConfig
import com.example.springjpalab.domain.event.OrderCreatedEvent
import com.example.springjpalab.domain.event.OrderStatusChangedEvent
import com.rabbitmq.client.Channel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class NotificationConsumer(
    private val notificationService: NotificationService,
    private val processedEventRepository: ProcessedEventJpaRepository,
    private val transactionTemplate: TransactionTemplate
) {
    private val logger = KotlinLogging.logger {}

    @RabbitListener(queues = [RabbitConfig.ORDER_CREATED_QUEUE])
    fun handleOrderCreated(
        event: OrderCreatedEvent,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long
    ) {
        try {
            runBlocking {
                notificationService.sendOrderCreatedAndWait(
                    to = event.userEmail,
                    orderId = event.orderId
                )
            }

            logger.info { "Order created notification sent for order ${event.orderId}" }
            channel.basicAck(deliveryTag, false)
        } catch (e: Exception) {
            logger.error(e) { "Failed to process order created event for order ${event.orderId}" }
            channel.basicNack(deliveryTag, false, true)
        }
    }

    @RabbitListener(queues = [RabbitConfig.ORDER_STATUS_QUEUE])
    fun handleOrderStatusChanged(
        event: OrderStatusChangedEvent,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long
    ) {
        try {
            transactionTemplate.executeWithoutResult {
                processedEventRepository.saveAndFlush(
                    ProcessedEventJpaEntity(
                        orderId = event.orderId,
                        newStatus = event.newStatus
                    )
                )

                runBlocking {
                    notificationService.sendOrderStatusUpdateAndWait(
                        to = event.userEmail,
                        orderId = event.orderId,
                        status = event.newStatus.toString()
                    )
                }
            }

            channel.basicAck(deliveryTag, false)
        } catch (e: DataIntegrityViolationException) {
            logger.warn { "Duplicate event for order ${event.orderId}, status ${event.newStatus}; skipping" }
            channel.basicAck(deliveryTag, false)
        } catch (e: Exception) {
            logger.error(e) { "Failed to process event for order ${event.orderId}" }
            channel.basicNack(deliveryTag, false, true)
        }
    }
}

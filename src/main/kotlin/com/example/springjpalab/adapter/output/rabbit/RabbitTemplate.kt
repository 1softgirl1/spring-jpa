package com.example.springjpalab.adapter.output.rabbit

import com.example.springjpalab.config.RabbitConfig
import com.example.springjpalab.domain.event.OrderCreatedEvent
import com.example.springjpalab.domain.event.OrderStatusChangedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class OrderEventPublisher(
    private val rabbitTemplate: RabbitTemplate
) {
    private val logger = KotlinLogging.logger {}

    fun publishOrderCreated(event: OrderCreatedEvent) {
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.ORDER_CREATED_ROUTING_KEY,
            event
        )
        logger.info { "Опубликовано событие OrderCreated для заказа ${event.orderId}" }
    }

    fun publishOrderStatusChanged(event: OrderStatusChangedEvent) {
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.ORDER_STATUS_ROUTING_KEY,
            event
        )
        logger.info { "Опубликовано событие OrderStatusChanged: ${event.oldStatus} → ${event.newStatus}" }
    }
}
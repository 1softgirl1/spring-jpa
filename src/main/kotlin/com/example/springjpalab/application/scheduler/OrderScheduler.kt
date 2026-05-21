package com.example.springjpalab.application.scheduler

import com.example.springjpalab.domain.model.OrderStatus
import com.example.springjpalab.adapter.output.jpa.repository.OrderJpaRepository
import com.example.springjpalab.application.service.NotificationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component
class OrderScheduler(
    private val orderRepository: OrderJpaRepository,
    private val notificationService: NotificationService
) {
    private val logger = KotlinLogging.logger {}

    @Value("\${spring.orders.preparing-timeout}")
    private lateinit var preparingTimeout: Duration

    @Scheduled(cron = "\${spring.orders.scheduler-cron}")
    fun cancelStuckOrders() {
        val thresholdTime = LocalDateTime.now().minus(preparingTimeout)
        val stuck = orderRepository.findByStatus(OrderStatus.PENDING)
        logger.info { "Найдено заказов для проверки на отмену: ${stuck.size}" }
        stuck.forEach {
            if(it.createdAt < thresholdTime) {
                if(it.status != OrderStatus.CANCELLED ){
                    it.status = OrderStatus.CANCELLED
                    orderRepository.save(it)
                    logger.info { "Заказ отменен шедулером: id=${it.id}, previousStatus=PENDING, newStatus=CANCELLED" }

                    val orderId = it.id
                    val user = it.user
                    notificationService.sendOrderStatusUpdate(
                        to = user.email,
                        orderId = orderId,
                        status = OrderStatus.CANCELLED.name
                    )
                }

            }
        }
    }
}


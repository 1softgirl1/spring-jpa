package com.example.springjpalab.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val mailSender: JavaMailSender,
    private val scope: CoroutineScope
) {
    private val logger = KotlinLogging.logger {}

    fun sendOrderStatusUpdate(to: String, orderId: Long, status: String) {
        scope.launch {
            runCatching {
                sendOrderStatusUpdateAndWait(to, orderId, status)
            }.onSuccess {
                logger.info { "Notification for order #$orderId sent to $to" }
            }.onFailure { ex ->
                logger.error(ex) { "Failed to send notification to $to" }
            }
        }
    }

    suspend fun sendOrderStatusUpdateAndWait(to: String, orderId: Long, status: String) {
        withContext(Dispatchers.IO) {
            mailSender.send(SimpleMailMessage().apply {
                setTo(to)
                subject = "Order #$orderId: status changed"
                text = "New status: $status"
            })
        }
    }

    suspend fun sendOrderCreatedAndWait(to: String, orderId: Long) {
        withContext(Dispatchers.IO) {
            mailSender.send(SimpleMailMessage().apply {
                setTo(to)
                subject = "Order #$orderId created"
                text = "Your order #$orderId has been created and is waiting for confirmation."
            })
        }
    }
}

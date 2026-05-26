package com.example.springjpalab.domain.event

import com.example.springjpalab.domain.model.OrderStatus
import java.time.LocalDateTime

data class OrderStatusChangedEvent(
    val orderId: Long,
    val userId: Long,
    val userEmail: String,
    val oldStatus: OrderStatus,
    val newStatus: OrderStatus,
    val changedAt: LocalDateTime = LocalDateTime.now()
)

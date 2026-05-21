package com.example.springjpalab.domain.event

import java.time.LocalDateTime

data class OrderCreatedEvent(
    val orderId: Long,
    val userId: Long,
    val dishIds: List<Long>,
    val userEmail: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
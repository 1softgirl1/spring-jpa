package com.example.springjpalab.domain.model

import com.example.springjpalab.adapter.output.jpa.entity.OrderStatus
import java.time.LocalDateTime

data class Order(
    val id: Long,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
    val userId: Long,
    val dishes: List<Dish>
)
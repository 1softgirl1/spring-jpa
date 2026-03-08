package com.example.springjpalab.domain.port

import com.example.springjpalab.adapter.output.jpa.entity.OrderJpaEntity
import com.example.springjpalab.adapter.output.jpa.entity.OrderStatus
import com.example.springjpalab.domain.model.Order


interface OrderRepositoryPort {
    fun findAll(): List<Order>
    fun findById(id: Long): Order?
    fun findByUserId(userId: Long): List<Order>
    fun findByStatus(status: OrderStatus): List<Order>
    fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order>
    fun findEntityById(id: Long): OrderJpaEntity?
    fun create(order: Order): Order
    fun update(order: Order): Order
}
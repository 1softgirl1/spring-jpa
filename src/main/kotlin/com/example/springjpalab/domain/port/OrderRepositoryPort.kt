package com.example.springjpalab.domain.port

import com.example.springjpalab.domain.model.Order
import com.example.springjpalab.domain.model.OrderStatus


interface OrderRepositoryPort {
    fun findAll(): List<Order>
    fun findById(id: Long): Order?
    fun findByUserId(userId: Long): List<Order>
    fun findByStatus(status: OrderStatus): List<Order>
    fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order>
    fun findByDishId(dishId: Long): List<Order>
    fun create(order: Order): Order
    fun update(order: Order): Order
}

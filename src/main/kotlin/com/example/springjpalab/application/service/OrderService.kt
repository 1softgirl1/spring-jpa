package com.example.springjpalab.application.service

import com.example.springjpalab.adapter.output.jpa.entity.OrderJpaEntity
import com.example.springjpalab.adapter.output.jpa.entity.OrderStatus
import com.example.springjpalab.domain.model.Order
import com.example.springjpalab.domain.port.OrderRepositoryPort
import org.springframework.stereotype.Service

@Service
class OrderService (
    private val repository: OrderRepositoryPort
) {
    fun findAll(): List<Order> = repository.findAll()
    fun findById(id: Long): Order? = repository.findById(id)
    fun findByUserId(userId: Long): List<Order>? = repository.findByUserId(userId)
    fun findByStatus(status: OrderStatus): List<Order>? = repository.findByStatus(status)
    fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order>?
            = repository.findByUserIdAndStatus(userId, status)
    fun create(order: Order): Order {
        val newOrd = order.copy(id = 0)
        return repository.create(newOrd)
    }
    fun update(id: Long, order: Order): Order {
        val existing = repository.findById(id)
            ?: throw IllegalArgumentException("Order not found")

        val currentStatus = existing.status
        val newStatus = order.status

        val validTransition =
            (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) ||
                    (currentStatus == OrderStatus.CONFIRMED && newStatus == OrderStatus.DELIVERED) ||
                    (currentStatus == newStatus)

        if (!validTransition) {
            throw IllegalStateException("Invalid status transition")
        }

        val updOrd = order.copy(id = id)
        return repository.update(updOrd)
    }
    fun findEntityById(id: Long): OrderJpaEntity? {
        return repository.findEntityById(id)
    }

}
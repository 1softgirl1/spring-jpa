package com.example.springjpalab.application.service

import com.example.springjpalab.adapter.output.jpa.entity.OrderJpaEntity
import com.example.springjpalab.adapter.output.jpa.entity.OrderStatus
import com.example.springjpalab.domain.exception.InvalidOrderStateException
import com.example.springjpalab.domain.exception.NotFoundException
import com.example.springjpalab.domain.model.Order
import com.example.springjpalab.domain.port.OrderRepositoryPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class OrderService (
    private val repository: OrderRepositoryPort
) {
    private val logger = KotlinLogging.logger {}

    fun findAll(): List<Order> =
        repository.findAll()

    fun findById(id: Long): Order =
        repository.findById(id) ?: run {
            logger.warn { "Заказ с id=$id не найден" }
            throw NotFoundException("Заказ с id=$id не найден")
        }

    fun findByUserId(userId: Long): List<Order> =
        repository.findByUserId(userId)


    fun findByStatus(status: OrderStatus): List<Order> =
        repository.findByStatus(status)


    fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order> =
        repository.findByUserIdAndStatus(userId, status)

    fun create(order: Order): Order {
        val newOrd = order.copy(id = 0)
        val saved = repository.create(newOrd)
        logger.info { "Создан заказ: id=${saved.id}, userId=${saved.userId}, status=${saved.status}" }
        return saved
    }
    fun update(id: Long, order: Order): Order {
        val existing = repository.findById(id) ?: run {
            logger.warn { "Заказ с id=$id не найден" }
            throw NotFoundException("Заказ с id=$id не найден")
        }

        val currentStatus = existing.status
        val newStatus = order.status

        val validTransition =
            (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) ||
                    (currentStatus == OrderStatus.CONFIRMED && newStatus == OrderStatus.DELIVERED) ||
                    (currentStatus == newStatus)

        if (!validTransition) {
            throw InvalidOrderStateException("Неверный переход статуса из $currentStatus в $newStatus")
        }

        val updOrd = order.copy(id = id)
        val saved = repository.update(updOrd)
        logger.info { "Обновлен заказ: id=${saved.id}, status=${saved.status}" }
        return saved
    }
    fun findEntityById(id: Long): OrderJpaEntity? {
        return repository.findEntityById(id)
            ?: throw NotFoundException("Заказ с id=$id не найден")
    }

}
package com.example.springjpalab.application.service

import com.example.springjpalab.adapter.output.rabbit.OrderEventPublisher
import com.example.springjpalab.domain.event.OrderCreatedEvent
import com.example.springjpalab.domain.event.OrderStatusChangedEvent
import com.example.springjpalab.domain.exception.InvalidOrderStateException
import com.example.springjpalab.domain.exception.NotFoundException
import com.example.springjpalab.domain.model.Order
import com.example.springjpalab.domain.model.OrderStatus
import com.example.springjpalab.domain.port.DishRepositoryPort
import com.example.springjpalab.domain.port.OrderRepositoryPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OrderService (
    private val repository: OrderRepositoryPort,
    private val dishRepository: DishRepositoryPort,
    private val userService: UserService,
    private val eventPublisher: OrderEventPublisher

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

    fun findByDishId(dishId: Long): List<Order> =
        repository.findByDishId(dishId)

    fun getOrdersForRequester(
        requestedUserId: Long?,
        status: OrderStatus?,
        requesterId: Long,
        isAdmin: Boolean
    ): List<Order> {
        if (!isAdmin && requestedUserId != null && requestedUserId != requesterId) {
            throw AccessDeniedException("Доступ запрещен")
        }

        val effectiveUserId = if (isAdmin) requestedUserId else requesterId

        return when {
            effectiveUserId != null && status != null -> findByUserIdAndStatus(effectiveUserId, status)
            effectiveUserId != null -> findByUserId(effectiveUserId)
            status != null -> findByStatus(status)
            else -> findAll()
        }
    }

    fun createForUser(userId: Long, dishIds: List<Long>): Order {
        val dishes = dishIds.map { dishId ->
            dishRepository.findById(dishId)
                ?: throw IllegalArgumentException("dishIds: Блюдо с id=$dishId не найдено")
        }

        return create(
            Order(
                id = 0,
                status = OrderStatus.PENDING,
                createdAt = LocalDateTime.now(),
                userId = userId,
                dishes = dishes
            )
        )
    }

    fun getByIdForRequester(orderId: Long, requesterId: Long, isAdmin: Boolean): Order {
        val order = findById(orderId)
        if (!isAdmin && order.userId != requesterId) {
            throw AccessDeniedException("Доступ запрещен")
        }
        return order
    }

    @Transactional
    fun create(order: Order): Order {
        val newOrd = order.copy(id = 0)
        val saved = repository.create(newOrd)
        val user = userService.findById(saved.userId)

        eventPublisher.publishOrderCreated(
            OrderCreatedEvent(
                orderId = saved.id,
                userId = saved.userId,
                dishIds = saved.dishes.map { it.id },
                userEmail = user.email,
                createdAt = saved.createdAt
            )
        )
        logger.info { "Создан заказ: id=${saved.id}, userId=${saved.userId}, status=${saved.status}" }
        return saved
    }

    @Transactional
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

        if (currentStatus != newStatus){
            val orderId = saved.id
            val userId = saved.userId
            val user = userService.findById(userId)

            eventPublisher.publishOrderStatusChanged(
                OrderStatusChangedEvent(
                    orderId = orderId,
                    userId = saved.userId,
                    userEmail = user.email,
                    oldStatus = currentStatus,
                    newStatus =newStatus,
                )
            )
        }
        logger.info { "Обновлен заказ: id=${saved.id}, status=${saved.status}" }
        return saved
    }
}

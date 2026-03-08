package com.example.springjpalab.adapter.output.jpa.adapter

import com.example.springjpalab.adapter.output.jpa.entity.DishJpaEntity
import com.example.springjpalab.adapter.output.jpa.entity.OrderJpaEntity
import com.example.springjpalab.adapter.output.jpa.entity.OrderStatus
import com.example.springjpalab.adapter.output.jpa.entity.RestaurantJpaEntity
import com.example.springjpalab.adapter.output.jpa.repository.DishJpaRepository
import com.example.springjpalab.adapter.output.jpa.repository.OrderJpaRepository
import com.example.springjpalab.adapter.output.jpa.repository.UserJpaRepository
import com.example.springjpalab.domain.model.Dish
import com.example.springjpalab.domain.model.Order
import com.example.springjpalab.domain.port.OrderRepositoryPort
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@Profile("db")
class OrderJpaAdapter (
    private val repository: OrderJpaRepository,
    private val userRepository: UserJpaRepository,
    private val dishRepository: DishJpaRepository
) : OrderRepositoryPort {

    override fun findAll(): List<Order> =
        repository.findAll().map { it.toDomain() }

    override fun findById(id: Long): Order? {
        val entity = repository.findById(id).orElse(null)
        return entity?.toDomain()
    }

    override fun findByUserId(userId: Long): List<Order> =
        repository.findByUserId(userId).map { it.toDomain() }

    override fun findByStatus(status: OrderStatus): List<Order> =
        repository.findByStatus(status).map { it.toDomain() }

    override fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order> =
        repository.findByUserIdAndStatus(userId, status).map { it.toDomain() }



    override fun create(order: Order): Order {
        val user = userRepository.findById(order.userId)
            .orElseThrow { RuntimeException("User not found") }

        val dishes = dishRepository.findAllById(order.dishes.map { it.id })

        val entity = OrderJpaEntity(
            id = order.id,
            status = order.status,
            createdAt = LocalDateTime.now(),
            user = user,
            dishes = dishes

        )
        return repository.save(entity).toDomain()

    }
    override fun update(order: Order): Order {
        val existing = repository.findById(order.id ?: throw RuntimeException("Order id null"))
            .orElseThrow { RuntimeException("Order not found") }

        val user = userRepository.findById(order.userId)
            .orElseThrow { RuntimeException("User not found") }

        val dishes = dishRepository.findAllById(order.dishes.map { it.id })

        val updated = OrderJpaEntity(
            id = existing.id,
            status = order.status,
            createdAt = order.createdAt,
            user = user,
            dishes = dishes
        )

        return repository.save(updated).toDomain()


    }



}
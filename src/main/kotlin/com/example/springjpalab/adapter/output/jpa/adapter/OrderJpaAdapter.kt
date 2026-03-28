package com.example.springjpalab.adapter.output.jpa.adapter

import com.example.springjpalab.adapter.output.jpa.entity.OrderJpaEntity
import com.example.springjpalab.adapter.output.jpa.entity.OrderStatus
import com.example.springjpalab.adapter.output.jpa.repository.DishJpaRepository
import com.example.springjpalab.adapter.output.jpa.repository.OrderJpaRepository
import com.example.springjpalab.adapter.output.jpa.repository.UserJpaRepository
import com.example.springjpalab.domain.model.Order
import com.example.springjpalab.domain.port.OrderRepositoryPort
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@Profile("test", "db")
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
    override fun findEntityById(id: Long): OrderJpaEntity? =
        repository.findById(id).orElse(null)


    override fun findByUserId(userId: Long): List<Order> =
        repository.findByUserId(userId).map { it.toDomain() }

    override fun findByStatus(status: OrderStatus): List<Order> =
        repository.findByStatus(status).map { it.toDomain() }

    override fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order> =
        repository.findByUserIdAndStatus(userId, status).map { it.toDomain() }



    override fun create(order: Order): Order {
        val user = userRepository.findById(order.userId).orElse(null)
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
        val entity = repository.findById(order.id).orElseThrow {
            NoSuchElementException("Order with id=${order.id} not found")
        }


        val user = userRepository.findById(order.userId).orElse(null) 

        val dishes = dishRepository.findAllById(order.dishes.map { it.id })

        val updated = OrderJpaEntity(
            id = entity.id,
            status = order.status,
            createdAt = order.createdAt,
            user = user,
            dishes = dishes
        )

        return repository.save(updated).toDomain()


    }
    

}
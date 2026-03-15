package com.example.springjpalab.application.service

import com.example.springjpalab.adapter.output.jpa.entity.DishJpaEntity
import com.example.springjpalab.adapter.output.jpa.entity.OrderJpaEntity
import com.example.springjpalab.domain.exception.AlreadyExistsException
import com.example.springjpalab.domain.exception.NotFoundException
import com.example.springjpalab.domain.model.Dish
import com.example.springjpalab.domain.port.DishRepositoryPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class DishService(
    private val repository: DishRepositoryPort,
    private val orderService: OrderService
) {
    private val logger = KotlinLogging.logger {}

    fun findByName(name: String): Dish? =
        repository.findByName(name)

    fun findByNamePart(namePart: String):  List<Dish?>?  =
        repository.findByNamePart(namePart)

    fun findAll(): List<Dish> {
        return repository.findAll()
    }

    fun findById(id: Long): Dish =
        repository.findById(id) ?: run {
            logger.warn { "Блюдо с id=$id не найдено" }
            throw NotFoundException("Блюдо с id=$id не найдено")
        }

    fun create(dish: Dish): Dish {
        repository.findByNameAndRestaurantId(dish.name, dish.restaurantId)?.let {
            logger.warn { "Блюдо '${dish.name}' уже существует" }
            throw AlreadyExistsException("Блюдо '${dish.name}' уже существует")
        }

        val saved = repository.create(dish)
        logger.info { "Создано блюдо: id=${saved.id}, name=${saved.name}" }
        return saved
    }

    fun update(id: Long, dish: Dish): Dish {
        repository.findById(id) ?: run {
            logger.warn { "Блюдо с id=$id не найдено" }
            throw NotFoundException("Блюдо с id=$id не найдено")
        }

        val updated = dish.copy(id = id)
        val saved = repository.update(updated)
        logger.info { "Обновлено блюдо: id=${saved.id}, name=${saved.name}" }
        return saved
    }

    fun delete(id: Long) {
        val dishEntity: DishJpaEntity = repository.findEntityById(id)
            ?: run {
                logger.warn { "Блюдо с id=$id не найдено" }
                throw NotFoundException("Блюдо с id=$id не найдено")
            }

        val ordersToUpdate: List<OrderJpaEntity> = dishEntity.orders.toList()
        for (orderJpa in ordersToUpdate) {
            orderJpa.dishes.remove(dishEntity)
            orderService.update(orderJpa.id, orderJpa.toDomain())
        }

        repository.deleteById(id)
        logger.info { "Удалено блюдо: id=$id" }
    }
}


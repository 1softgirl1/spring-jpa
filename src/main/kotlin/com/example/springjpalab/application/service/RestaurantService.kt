package com.example.springjpalab.application.service

import com.example.springjpalab.domain.exception.AlreadyExistsException
import com.example.springjpalab.domain.exception.NotFoundException
import com.example.springjpalab.domain.model.Restaurant
import com.example.springjpalab.domain.port.RestaurantRepositoryPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class RestaurantService (
    private val repository: RestaurantRepositoryPort
) {
    private val logger = KotlinLogging.logger {}

    fun findAll(): List<Restaurant> =
        repository.findAll()

    fun findById(id: Long): Restaurant =
        repository.findById(id) ?: run {
            logger.warn { "Ресторан с id=$id не найден" }
            throw NotFoundException("Ресторан с id=$id не найден")
        }

    fun findByName(name: String): Restaurant =
        repository.findByName(name) ?: run {
            logger.warn { "Ресторан с name=$name не найден" }
            throw NotFoundException("Ресторан с name=$name не найден")
        }

    fun create(restaurant: Restaurant): Restaurant {
        val existing = repository.findByName(restaurant.name)
        if (existing != null) {
            logger.warn { "Ресторан '${restaurant.name}' уже существует" }
            throw AlreadyExistsException("Ресторан '${restaurant.name}' уже существует")
        }

        val newRestaurant = restaurant.copy(id = 0)
        val saved = repository.create(newRestaurant)
        logger.info { "Создан ресторан: id=${saved.id}, name=${saved.name}" }
        return saved
    }

    fun update(id: Long, restaurant: Restaurant): Restaurant {

        val toUpdate = restaurant.copy(id = id)
        val updated = repository.update(toUpdate)
        if (updated == null) {
            logger.warn { "Ресторан с id=$id не найден" }
            throw NotFoundException("Ресторан с id=$id не найден")
        }
        logger.info { "Обновлен ресторан: id=${updated.id}, name=${updated.name}" }
        return updated
    }

    fun delete(id: Long) {
        val deleted = repository.deleteById(id)
        if (!deleted) {
            logger.warn { "Ресторан с id=$id не найден" }
            throw NotFoundException("Ресторан с id=$id не найден")
        }
        logger.info { "Удален ресторан: id=$id" }
    }

}
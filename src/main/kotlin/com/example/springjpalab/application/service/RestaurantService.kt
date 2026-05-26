package com.example.springjpalab.application.service

import com.example.springjpalab.domain.exception.AlreadyExistsException
import com.example.springjpalab.domain.exception.NotFoundException
import com.example.springjpalab.domain.model.Dish
import com.example.springjpalab.domain.model.Restaurant
import com.example.springjpalab.domain.port.RestaurantRepositoryPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class RestaurantService(
    private val repository: RestaurantRepositoryPort
) {
    private val logger = KotlinLogging.logger {}

    @Cacheable(cacheNames = ["restaurants"])
    fun findAll(): List<Restaurant> {
        logger.info { "Промах кэша restaurants.findAll: загружаем из репозитория" }
        return repository.findAll()
    }

    @Cacheable(cacheNames = ["restaurants"], key = "#id")
    fun findById(id: Long): Restaurant {
        logger.info { "Промах кэша restaurants.findById для id=$id: загружаем из репозитория" }
        return repository.findById(id) ?: run {
            logger.warn { "Ресторан с id=$id не найден" }
            throw NotFoundException("Ресторан с id=$id не найден")
        }
    }

    @Cacheable(cacheNames = ["restaurants"], key = "'byName:' + #name")
    fun findByName(name: String): Restaurant {
        logger.info { "Промах кэша restaurants.findByName для name=$name: загружаем из репозитория" }
        return repository.findByName(name) ?: run {
            logger.warn { "Ресторан с name=$name не найден" }
            throw NotFoundException("Ресторан с name=$name не найден")
        }
    }

    @CacheEvict(cacheNames = ["restaurants"], allEntries = true)
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

    @CacheEvict(cacheNames = ["restaurants"], allEntries = true)
    fun createRestaurant(name: String, address: String): Restaurant =
        create(
            Restaurant(
                id = 0,
                name = name,
                address = address,
                dishes = emptyList()
            )
        )

    @CachePut(cacheNames = ["restaurants"], key = "#id")
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

    @CachePut(cacheNames = ["restaurants"], key = "#id")
    fun updateRestaurant(id: Long, name: String, address: String): Restaurant {
        val existing = findById(id)
        return update(
            id,
            existing.copy(
                name = name,
                address = address
            )
        )
    }

    fun getRestaurantDishes(id: Long): List<Dish> =
        findById(id).dishes

    @CacheEvict(cacheNames = ["restaurants"], allEntries = true)
    fun delete(id: Long) {
        val deleted = repository.deleteById(id)
        if (!deleted) {
            logger.warn { "Ресторан с id=$id не найден" }
            throw NotFoundException("Ресторан с id=$id не найден")
        }
        logger.info { "Удален ресторан: id=$id" }
    }
}

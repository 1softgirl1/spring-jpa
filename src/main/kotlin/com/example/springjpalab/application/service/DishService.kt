package com.example.springjpalab.application.service

import com.example.springjpalab.adapter.output.jpa.entity.DishJpaEntity
import com.example.springjpalab.adapter.output.jpa.entity.OrderJpaEntity
import com.example.springjpalab.domain.exception.AlreadyExistsException
import com.example.springjpalab.domain.exception.NotFoundException
import com.example.springjpalab.domain.model.Dish
import com.example.springjpalab.domain.port.DishRepositoryPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class DishService(
    private val repository: DishRepositoryPort,
    private val orderService: OrderService,
    private val restaurantService: RestaurantService
) {
    private val logger = KotlinLogging.logger {}

    @Cacheable(cacheNames = ["dishes"], key = "'byName:' + #name")
    fun findByName(name: String): Dish? {
        logger.info { "Cache miss for dishes.byName name='$name': loading from repository" }
        return repository.findByName(name)
    }

    @Cacheable(cacheNames = ["dishes"], key = "'byNamePart:' + #namePart")
    fun findByNamePart(namePart: String): List<Dish?>? {
        logger.info { "Cache miss for dishes.byNamePart namePart='$namePart': loading from repository" }
        return repository.findByNamePart(namePart)
    }

    @Cacheable(cacheNames = ["dishes"], key = "#namePart ?: 'all'")
    fun getDishes(namePart: String?): List<Dish> {
        return if (namePart.isNullOrBlank()) {
            findAll()
        } else {
            findByNamePart(namePart).orEmpty().filterNotNull()
        }
    }

    @Cacheable(cacheNames = ["dishes"])
    fun findAll(): List<Dish> {
        logger.info { "Cache miss for dishes.findAll: loading from repository" }
        return repository.findAll()
    }

    @Cacheable(cacheNames = ["dishes"], key = "#id")
    fun findById(id: Long): Dish {
        logger.info { "Cache miss for dishes.byId id=$id: loading from repository" }
        return repository.findById(id) ?: run {
            logger.warn { "Блюдо с id=$id не найдено" }
            throw NotFoundException("Блюдо с id=$id не найдено")
        }
    }

    @Caching(evict = [
        CacheEvict(cacheNames = ["dishes"], allEntries = true),
        CacheEvict(cacheNames = ["restaurants"], allEntries = true)
    ])
    fun create(dish: Dish): Dish {
        repository.findByNameAndRestaurantId(dish.name, dish.restaurantId)?.let {
            logger.warn { "Блюдо '${dish.name}' уже существует" }
            throw AlreadyExistsException("Блюдо '${dish.name}' уже существует")
        }

        val saved = repository.create(dish)
        logger.info { "Создано блюдо: id=${saved.id}, name=${saved.name}" }
        return saved
    }

    @Deprecated("Use create(Dish)")
    fun createInRestaurant(
        restaurantId: Long,
        name: String,
        description: String,
        price: BigDecimal,
        isAvailable: Boolean
    ): Dish {
        restaurantService.findById(restaurantId)

        return create(
            Dish(
                id = 0,
                name = name,
                description = description,
                price = price,
                isAvailable = isAvailable,
                restaurantId = restaurantId
            )
        )
    }

    @Caching(evict = [
        CacheEvict(cacheNames = ["dishes"], allEntries = true),
        CacheEvict(cacheNames = ["restaurants"], allEntries = true)
    ])
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

    @Deprecated("Use update(Dish)")
    fun updateDish(
        id: Long,
        name: String,
        description: String,
        price: BigDecimal,
        isAvailable: Boolean
    ): Dish {
        val existing = findById(id)
        return update(
            id,
            existing.copy(
                name = name,
                description = description,
                price = price,
                isAvailable = isAvailable
            )
        )
    }

    @Caching(evict = [
        CacheEvict(cacheNames = ["dishes"], allEntries = true),
        CacheEvict(cacheNames = ["restaurants"], allEntries = true)
    ])
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

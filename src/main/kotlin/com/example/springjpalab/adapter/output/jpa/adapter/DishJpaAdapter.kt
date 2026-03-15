package com.example.springjpalab.adapter.output.jpa.adapter

import com.example.springjpalab.adapter.output.jpa.entity.DishJpaEntity
import com.example.springjpalab.adapter.output.jpa.repository.DishJpaRepository
import com.example.springjpalab.adapter.output.jpa.repository.RestaurantJpaRepository
import com.example.springjpalab.domain.model.Dish
import com.example.springjpalab.domain.port.DishRepositoryPort
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.collections.map

@Component
@Profile("db")
class DishJpaAdapter(
    private val repository: DishJpaRepository,
    private val restaurantRepository: RestaurantJpaRepository
) : DishRepositoryPort {

    override fun findAll(): List<Dish> =
        repository.findAll().map { it.toDomain() }

    override fun findById(id: Long): Dish? =
        repository.findById(id).orElse(null)?.toDomain()

    override fun findByName(name: String): Dish? =
        repository.findByName(name)?.toDomain()

    override fun findEntityById(id: Long): DishJpaEntity? =
        repository.findById(id).orElse(null)

    override fun findByNamePart(namePart: String): List<Dish?>? =
        repository.findByNamePart(namePart).map { it.toDomain() }

    override fun findByNameAndRestaurantId(name: String, restaurantId: Long): Dish? =
        repository.findByNameAndRestaurantId(name, restaurantId)?.toDomain()


    override fun create(dish: Dish): Dish {
        val restaurantEntity = restaurantRepository.findById(dish.restaurantId).orElse(null)

        val entity = DishJpaEntity(
            name = dish.name,
            description = dish.description,
            price = dish.price,
            isAvailable = dish.isAvailable,
            restaurant = restaurantEntity
        )

        return repository.save(entity).toDomain()
    }

    override fun update(dish: Dish): Dish {
        val restaurantEntity = restaurantRepository.findById(dish.restaurantId).orElse(null)
        val existing = repository.findById(dish.id)
        if (existing.isEmpty) {
            throw NoSuchElementException("Dish with id=${dish.id} not found")
        }

        val entity = existing.get()

        val updatedEntity = DishJpaEntity(
            id = entity.id,
            name = dish.name,
            description = dish.description,
            price = dish.price,
            isAvailable = dish.isAvailable,
            restaurant = restaurantEntity
        )

        return repository.save(updatedEntity).toDomain()
    }

    override fun deleteById(id: Long): Boolean {
        if (!repository.existsById(id)) return false

        repository.deleteById(id)
        return true
    }
}
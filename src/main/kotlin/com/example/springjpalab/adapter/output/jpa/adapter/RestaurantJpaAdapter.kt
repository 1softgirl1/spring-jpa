package com.example.springjpalab.adapter.output.jpa.adapter

import com.example.springjpalab.adapter.output.jpa.entity.RestaurantJpaEntity
import com.example.springjpalab.adapter.output.jpa.repository.RestaurantJpaRepository
import com.example.springjpalab.domain.model.Restaurant
import com.example.springjpalab.domain.port.RestaurantRepositoryPort
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component


@Component
@Profile("db", "test")
class RestaurantJpaAdapter (
    private val repository: RestaurantJpaRepository
): RestaurantRepositoryPort
{
    override fun findAll(): List<Restaurant> =
        repository.findAll().map { it.toDomain() }

    override fun findById(id: Long): Restaurant? =
        repository.findByIdFetchDishes(id)?.toDomain()

    override fun findByName(name: String): Restaurant? =
        repository.findByName(name)?.toDomain()


    override fun create(restaurant: Restaurant): Restaurant {
        val entity = RestaurantJpaEntity(
            name = restaurant.name,
            address = restaurant.address
        )
        return repository.save(entity).toDomain()
    }

    override fun update(restaurant: Restaurant): Restaurant? {
        val existing = repository.findById(restaurant.id)

        val entity = repository.findById(restaurant.id).orElseThrow {
            NoSuchElementException("Restaurant with id=${restaurant.id} not found")
        }

        val updated = RestaurantJpaEntity(
            id = entity.id,
            name = restaurant.name,
            address = restaurant.address,
            dishes = entity.dishes
        )

        return repository.save(updated).toDomain()
    }


    override fun deleteById(id: Long): Boolean {
        if (!repository.existsById(id)) return false

        repository.deleteById(id)
        return true
    }
}
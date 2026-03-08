package com.example.springjpalab.application.service

import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantResponse
import com.example.springjpalab.adapter.output.jpa.entity.RestaurantJpaEntity
import com.example.springjpalab.adapter.output.jpa.repository.RestaurantJpaRepository
import com.example.springjpalab.domain.model.Restaurant
import com.example.springjpalab.domain.port.RestaurantRepositoryPort
import org.springframework.stereotype.Service

@Service
class RestaurantService (
    private val repository: RestaurantRepositoryPort
) {
    fun findAll(): List<Restaurant> =
        repository.findAll()

    fun findById(id: Long): Restaurant? =
        repository.findById(id)

    fun findByName(name: String): Restaurant? =
        repository.findByName(name)

    fun create(restaurant: Restaurant): Restaurant =
        repository.create(restaurant)

    fun update(id: Long, restaurant: Restaurant): Restaurant {
        val toUpdate = restaurant.copy(id = id)
        return repository.update(toUpdate)
    }

    fun delete(id: Long): Boolean =
        repository.deleteById(id)

}
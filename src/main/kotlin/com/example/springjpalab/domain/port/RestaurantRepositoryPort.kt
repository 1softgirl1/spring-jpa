package com.example.springjpalab.domain.port

import com.example.springjpalab.domain.model.Restaurant


interface RestaurantRepositoryPort {
    fun findAll(): List<Restaurant>
    fun findById(id: Long): Restaurant?
    fun findByName(name: String): Restaurant?
    fun create(restaurant: Restaurant): Restaurant
    fun update(restaurant: Restaurant): Restaurant
    fun deleteById(id: Long): Boolean

}
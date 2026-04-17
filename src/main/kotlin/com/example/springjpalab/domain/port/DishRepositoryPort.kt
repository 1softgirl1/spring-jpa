package com.example.springjpalab.domain.port
import com.example.springjpalab.adapter.output.jpa.entity.DishJpaEntity
import com.example.springjpalab.domain.model.Dish



interface DishRepositoryPort {
    fun findByName(name: String): Dish?
    fun findByNamePart(namePart: String): List<Dish?>?
    fun findById(id: Long): Dish?
    fun findEntityById(id: Long): DishJpaEntity?
    fun findByNameAndRestaurantId(name: String, restaurantId: Long): Dish?
    fun findAll(): List<Dish>
    fun create(dish: Dish): Dish
    fun update(dish: Dish): Dish
    fun deleteById(id: Long): Boolean

}
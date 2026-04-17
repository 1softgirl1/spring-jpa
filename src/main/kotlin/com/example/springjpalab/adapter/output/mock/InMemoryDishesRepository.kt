package com.example.springjpalab.adapter.output.mock

import com.example.springjpalab.adapter.output.jpa.entity.DishJpaEntity
import com.example.springjpalab.domain.port.DishRepositoryPort
import com.example.springjpalab.domain.model.Dish
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import kotlin.collections.set

@Repository
@Profile("mock")
class InMemoryDishesRepository: DishRepositoryPort {
    private val dishes: MutableMap<Long, Dish> = mutableMapOf()
    private var seq = 1L

    override fun findByName(name: String): Dish? {
        return dishes.values.find { it.name == name }
    }

    override fun findByNamePart(namePart: String): List<Dish> {
        return dishes.values.filter { it.name.contains(namePart)
            it.name.contains(namePart, ignoreCase = true)
        }
    }

    override fun findAll(): List<Dish> {
        return dishes.values.toList()
    }

    override fun findById(id: Long) : Dish? {
        return dishes[id]
    }

    override fun create(dish: Dish): Dish{
        val saved = dish.copy(id = seq++)
        dishes[saved.id] = saved
        return saved
    }

    override fun update(dish: Dish): Dish {
        dishes[dish.id] = dish
        return dish
    }

    override fun deleteById(id: Long): Boolean {
        dishes.remove(id)
        return true
    }

    override fun findEntityById(id: Long): DishJpaEntity? {
        return null
    }

    override fun findByNameAndRestaurantId(name: String, restaurantId: Long): Dish? {
        TODO("Not yet implemented")
    }
}
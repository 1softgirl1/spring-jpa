package com.example.springjpalab.application.service

import com.example.springjpalab.adapter.output.jpa.entity.DishJpaEntity
import com.example.springjpalab.adapter.output.jpa.entity.OrderJpaEntity
import com.example.springjpalab.domain.model.Dish
import com.example.springjpalab.domain.port.DishRepositoryPort
import com.example.springjpalab.domain.port.OrderRepositoryPort
import org.springframework.stereotype.Service

@Service
class DishService(
    private val repository: DishRepositoryPort,
    private val orderRepository: OrderRepositoryPort
) {
    fun findByName(name: String): Dish? =
        repository.findByName(name)

    fun findByNamePart(namePart: String):  List<Dish?>?  =
        repository.findByNamePart(namePart)

    fun findAll(): List<Dish> {
        return repository.findAll()
    }

    fun findById(id: Long): Dish? =
        repository.findById(id)

    fun create(dish: Dish): Dish {
        val newDish = dish.copy(id = 0)
        return repository.create(newDish)
    }

    fun update(id: Long, dish: Dish): Dish {
        val updated = dish.copy(id = id)
        return repository.update(updated)
    }

    fun delete(id: Long) {
        val dishJpa: DishJpaEntity = repository.findEntityById(id) ?: return


        dishJpa.orders.forEach { orderJpa: OrderJpaEntity ->
            orderJpa.dishes.remove(dishJpa)
            orderRepository.update(orderJpa.toDomain())
        }

        // Удаляем само блюдо
        repository.deleteById(id)
    }
}


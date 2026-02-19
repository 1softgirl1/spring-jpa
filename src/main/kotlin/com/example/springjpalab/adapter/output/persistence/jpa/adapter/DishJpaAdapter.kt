package com.example.springjpalab.adapter.output.persistence.jpa.adapter

import com.example.springjpalab.adapter.output.persistence.jpa.entity.DishJpaEntity
import com.example.springjpalab.adapter.output.persistence.jpa.repository.DishJpaRepository
import com.example.springjpalab.domain.model.Dish
import com.example.springjpalab.domain.port.DishRepositoryPort
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@Profile("db")
class DishJpaAdapter(
    private val repository: DishJpaRepository
) : DishRepositoryPort  {
    override fun create(dish: Dish): Dish {
        val entity = DishJpaEntity(
            name = dish.name,
            description = dish.description,
            price = dish.price,
            isAvailable = dish.isAvailable
        )
        return repository.save(entity).toDomain()
    }

    override fun update(dish: Dish): Dish {
        val existing = repository.findById(dish.id ?: throw IllegalArgumentException("Dish id is null"))
            .orElseThrow { IllegalArgumentException("Dish not found with id ${dish.id}") }

        val updated = existing.copy(
            name = dish.name,
            description = dish.description,
            price = dish.price,
            isAvailable = dish.isAvailable
        )
        return repository.save(updated).toDomain()
    }

    override fun findById(id: Long): Dish? =
        repository.findById(id).orElse(null)?.toDomain()

    override fun findAll(): List<Dish> {
        return repository.findAll().map { it.toDomain() }
    }

    override fun deleteById(id: Long) =
        repository.deleteById(id)

    override fun findByName(name: String): Dish?  =
        repository.findByName(name)?.toDomain()


    override fun findByNamePart(namePart: String): List<Dish?>? =
        repository.findByNamePart(namePart)?.map { it.toDomain() }


    private fun DishJpaEntity.toDomain() = Dish(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price as BigDecimal,
        isAvailable = this.isAvailable
    )
}
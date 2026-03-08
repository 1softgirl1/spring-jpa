package com.example.springjpalab.adapter.output.jpa.entity

import com.example.springjpalab.domain.model.Dish
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "dishes")
class DishJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false)
    val description: String = "",

    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val isAvailable: Boolean = true,

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    val restaurant: RestaurantJpaEntity = RestaurantJpaEntity() // дефолтное значение
) {
    fun toDomain(): Dish {
        return Dish(
            id = this.id,
            name = this.name,
            description = this.description,
            price = this.price,
            isAvailable = this.isAvailable,
            restaurantId = this.restaurant.id
        )
    }
}
package com.example.springjpalab.adapter.output.jpa.entity

import com.example.springjpalab.domain.model.Restaurant
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "restaurants")
class RestaurantJpaEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false)
    val address: String = "",

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true)
    val dishes: List<DishJpaEntity> = mutableListOf()

    )
{
    fun toDomain(): Restaurant {
        return Restaurant(
            id = this.id,
            name = this.name,
            address = this.address,
            dishes = this.dishes.map { it.toDomain() }
        )
    }
}
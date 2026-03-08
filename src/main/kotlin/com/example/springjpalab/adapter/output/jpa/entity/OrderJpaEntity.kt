package com.example.springjpalab.adapter.output.jpa.entity

import com.example.springjpalab.domain.model.Order
import com.example.springjpalab.domain.model.Restaurant
import jakarta.persistence.*
import java.time.LocalDateTime



@Entity
@Table(name = "orders")
class OrderJpaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderStatus = OrderStatus.PENDING,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserJpaEntity = UserJpaEntity(),

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "order_dishes",
        joinColumns = [JoinColumn(name = "order_id")],
        inverseJoinColumns = [JoinColumn(name = "dish_id")]
    )
    var dishes: MutableList<DishJpaEntity> = mutableListOf()

) {

    fun toDomain(): Order {
        return Order(
            id = this.id,
            status = this.status,
            createdAt = this.createdAt,
            userId = this.user.id,
            dishes = this.dishes.map { it.toDomain() }
        )
    }
}
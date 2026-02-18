package com.example.springjpalab.adapter.output.persistence.jpa.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class UserJpaEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val email: String = "",

    @Column(nullable = false)
    val firstName: String = "",

    @Column(nullable = false)
    val lastName: String = "",

    @Column(nullable = false)
    val isActive: Boolean = true,

)

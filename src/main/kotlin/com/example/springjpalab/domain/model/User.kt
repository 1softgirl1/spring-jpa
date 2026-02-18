package com.example.springjpalab.domain.model

data class User (
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean,
)
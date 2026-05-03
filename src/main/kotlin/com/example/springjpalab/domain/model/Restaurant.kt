package com.example.springjpalab.domain.model

data class Restaurant(
    val id: Long,
    var name: String,
    var address: String,
    val dishes: List<Dish>
)
package com.example.springjpalab.adapter.input.dto.dish

import com.fasterxml.jackson.annotation.JsonGetter

data class DishResponse(

    val id: Long,
    val name: String,
    val description: String,
    val price: Number,
    @get:JsonGetter("isAvailable")
    val isAvailable: Boolean,
    val restaurantId: Long
)

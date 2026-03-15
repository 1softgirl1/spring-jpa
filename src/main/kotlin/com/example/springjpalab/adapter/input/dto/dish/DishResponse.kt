package com.example.springjpalab.adapter.input.dto.dish

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class DishResponse(

    val id: Long,
    val name: String,
    val description: String,
    val price: Number,
    val isAvailable: Boolean,
    val restaurantId: Long
)

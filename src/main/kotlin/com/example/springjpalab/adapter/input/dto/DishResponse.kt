package com.example.springjpalab.adapter.input.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class DishResponse(

    @field:NotNull(message = "Id cannot be null")
    val id: Long,

    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:NotBlank(message = "Description cannot be blank")
    val description: String,

    @field:NotNull(message = "Price cannot be null")
    val price: Number,

    @field:NotNull(message = "Availability must be specified")
    val isAvailable: Boolean
)

package com.example.springjpalab.adapter.input.dto.restaurant

import jakarta.validation.constraints.NotBlank

class RestaurantCreateRequest (
    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:NotBlank(message = "Address cannot be blank")
    val address: String,
)
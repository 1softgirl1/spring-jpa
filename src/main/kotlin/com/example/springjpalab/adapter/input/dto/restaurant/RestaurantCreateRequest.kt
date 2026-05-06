package com.example.springjpalab.adapter.input.dto.restaurant

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class RestaurantCreateRequest (

    @field:NotBlank(message = "Name cannot be blank")
    @field:Size(min = 1, message = "Name must be at least 1 character")
    val name: String,

    @field:NotBlank(message = "Address cannot be blank")
    @field:Size(min = 1, message = "Address must be at least 1 character")
    val address: String,
)
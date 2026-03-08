package com.example.springjpalab.adapter.input.dto.restaurant

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class RestaurantResponse (

    @field:NotNull(message = "Id cannot be null")
    val id: Long,

    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:NotBlank(message = "Address cannot be blank")
    val address: String,

)
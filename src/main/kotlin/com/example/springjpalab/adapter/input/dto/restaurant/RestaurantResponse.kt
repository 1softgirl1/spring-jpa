package com.example.springjpalab.adapter.input.dto.restaurant

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class RestaurantResponse (

    val id: Long,
    val name: String,
    val address: String,

)
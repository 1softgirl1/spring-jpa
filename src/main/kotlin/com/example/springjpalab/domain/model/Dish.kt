package com.example.springjpalab.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class Dish(
    val id: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
    @param:JsonProperty("isAvailable")
    val isAvailable: Boolean = false,
    val restaurantId: Long
)

package com.example.springjpalab.adapter.input.dto.restaurant

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "DTO ресторана")
class RestaurantResponse (

    @field:Schema(
        description = "Уникальный идентификатор ресторана",
        example = "0"
    )
    val id: Long,

    @field:Schema(
        description = "Название ресторана",
        example = "La Piazza"
    )
    val name: String,

    @field:Schema(
        description = "Адрес ресторана",
        example = "Москва, ул. Тверская, 10"
    )
    val address: String,

)
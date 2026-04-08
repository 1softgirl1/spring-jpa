package com.example.springjpalab.adapter.input.mapper

import com.example.springjpalab.adapter.input.dto.dish.DishResponse
import com.example.springjpalab.domain.model.Dish

fun Dish.toResponse(): DishResponse =
    DishResponse(
        id = id,
        name = name,
        description = description,
        price = price,
        isAvailable = isAvailable,
        restaurantId = restaurantId
    )


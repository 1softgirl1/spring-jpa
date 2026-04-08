package com.example.springjpalab.adapter.input.mapper

import com.example.springjpalab.adapter.input.dto.dish.DishResponse
import com.example.springjpalab.adapter.input.dto.order.OrderResponse
import com.example.springjpalab.domain.model.Order

fun Order.toResponse(): OrderResponse =
    OrderResponse(
        id = id,
        status = status,
        createdAt = createdAt.toString(),
        userId = userId,
        dishes = dishes.map { dish ->
            DishResponse(
                id = dish.id,
                name = dish.name,
                description = dish.description,
                price = dish.price,
                isAvailable = dish.isAvailable,
                restaurantId = dish.restaurantId
            )
        }
    )


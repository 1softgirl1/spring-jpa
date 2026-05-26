package com.example.springjpalab.adapter.input.mapper

import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantResponse
import com.example.springjpalab.domain.model.Restaurant

fun Restaurant.toResponse(): RestaurantResponse =
    RestaurantResponse(
        id = id,
        name = name,
        address = address
    )


package com.example.springjpalab.adapter.input.dto.order

import com.example.springjpalab.adapter.input.dto.dish.DishResponse
import com.example.springjpalab.adapter.output.jpa.entity.OrderStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class OrderResponse (

    var id: Long,
    var userId: Long,
    val status: OrderStatus,
    val createdAt: String,
    var dishes: List<DishResponse>,

)
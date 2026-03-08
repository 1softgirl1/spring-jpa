package com.example.springjpalab.adapter.input.dto.order

import com.example.springjpalab.adapter.input.dto.dish.DishResponse
import com.example.springjpalab.adapter.output.jpa.entity.OrderStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class OrderResponse (

    @field:NotNull(message = "Id cannot be null")
    var id: Long,

    @field:NotNull(message = "Id cannot be null")
    var userId: Long,

    @field:NotBlank(message = "Status cannot be blank")
    val status: OrderStatus,

    @field:NotBlank(message = "createdAt cannot be blank")
    val createdAt: String,

    @field:NotNull(message = "Dishes must be specified")
    var dishes: List<DishResponse>,

)
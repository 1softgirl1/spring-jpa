package com.example.springjpalab.adapter.input.dto.order

import com.example.springjpalab.adapter.input.dto.dish.DishResponse
import jakarta.validation.constraints.NotNull

class OrderCreateRequest (

    @field:NotNull(message = "Id cannot be null")
    var userId: Long,

    @field:NotNull(message = "Dishes must be specified")
    var dishIds: List<Long>
)
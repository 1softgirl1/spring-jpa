package com.example.springjpalab.adapter.input.dto.order

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class OrderCreateRequest(
    @field:NotEmpty(message = "dishIds cannot be empty")
    var dishIds: List<Long> = emptyList()
)
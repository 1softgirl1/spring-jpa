package com.example.springjpalab.adapter.input.dto.order

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class OrderCreateRequest(
    @field:NotNull(message = "userId must be specified")
    @field:Min(value = 1, message = "userId must be greater than 0")
    var userId: Long? = null,

    @field:NotEmpty(message = "dishIds cannot be empty")
    var dishIds: List<Long> = emptyList()
)
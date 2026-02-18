package com.example.springjpalab.adapter.input.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ErrorResponse(
    @field:NotNull(message = "Status cannot be null")
    val status: Int,
    @field:NotBlank(message = "Error cannot be blank")
    val error: String,
    @field:NotBlank(message = "Message cannot be blank")
    val message: String,
)

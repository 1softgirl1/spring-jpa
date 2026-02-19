package com.example.springjpalab.adapter.input.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UserResponse (
    @field:NotNull(message = "Id cannot be null or empty")
    val id: Long,
    @field:NotBlank(message = "Email cannot be blank")
    val email: String,
    @field:NotBlank(message = "First name cannot be blank")
    val firstName: String,
    @field:NotBlank(message = "Last name cannot be blank")
    val lastName: String,

    @field:NotNull(message = "Active cannot be null or empty")
    val active: Boolean
)
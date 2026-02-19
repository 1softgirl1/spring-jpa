package com.example.springjpalab.adapter.input.dto.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class UserUpdateRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "First name cannot be blank")
    @field:Size(min = 1, message = "FirstName must be at least 1 character")
    val firstName: String,

    @field:NotBlank(message = "First name cannot be blank")
    @field:Size(min = 1, message = "FirstName must be at least 1 character")
    val lastName: String,

    @field:NotNull(message = "Active cannot be null or empty")
    val active: Boolean
)

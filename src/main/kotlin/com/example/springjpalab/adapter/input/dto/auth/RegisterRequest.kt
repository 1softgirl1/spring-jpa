package com.example.springjpalab.adapter.input.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest (
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 6, message = "Password be at least 6 character")
    val password: String,

    @field:NotBlank(message = "Name cannot be blank")
    @field:Size(min = 1, message = "Name must be at least 1 character")
    val name: String
)
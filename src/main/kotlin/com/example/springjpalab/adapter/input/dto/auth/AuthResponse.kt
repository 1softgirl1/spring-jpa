package com.example.springjpalab.adapter.input.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AuthResponse(
    val token: String,
    val email: String,
    val role: String
)
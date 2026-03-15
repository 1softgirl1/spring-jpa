package com.example.springjpalab.adapter.input.dto.user

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UserResponse (
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,

    @JsonProperty("active")
    val isActive: Boolean
)
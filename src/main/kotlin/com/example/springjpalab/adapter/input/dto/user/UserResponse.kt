package com.example.springjpalab.adapter.input.dto.user

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


@Schema(description = "DTO пользователя")
data class UserResponse (
    @field:Schema(
        description = "Уникальный идентификатор пользователя",
        example = "0"
    )
    val id: Long,

    @field:Schema(
        description = "Email",
        example = "user@example.com"
    )
    val email: String,

    @field:Schema(
        description = "Имя",
        example = "string"
    )
    val firstName: String,

    @field:Schema(
        description = "Фамилия",
        example = "string"
    )
    val lastName: String,

    @field:Schema(
        description = "Активен ли пользователь",
        example = true.toString()
    )
    @JsonProperty("isActive")
    val isActive: Boolean,

    @field:Schema(
        description = "Роль пользователя",
        example = "USER"
    )
    val role: String
)
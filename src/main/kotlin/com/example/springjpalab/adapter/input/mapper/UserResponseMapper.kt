package com.example.springjpalab.adapter.input.mapper

import com.example.springjpalab.adapter.input.dto.user.UserResponse
import com.example.springjpalab.domain.model.User

fun User.toResponse(): UserResponse =
    UserResponse(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        isActive = isActive,
        role = role.name
    )


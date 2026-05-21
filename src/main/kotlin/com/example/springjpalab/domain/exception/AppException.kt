package com.example.springjpalab.domain.exception

sealed class AppException(message: String) : RuntimeException(message)
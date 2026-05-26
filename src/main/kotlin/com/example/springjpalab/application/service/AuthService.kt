package com.example.springjpalab.application.service

import com.example.springjpalab.adapter.input.dto.auth.AuthResponse
import com.example.springjpalab.adapter.input.dto.auth.LoginRequest
import com.example.springjpalab.adapter.input.dto.auth.RegisterRequest

import com.example.springjpalab.adapter.output.jpa.entity.UserJpaEntity
import com.example.springjpalab.adapter.output.jpa.repository.UserJpaRepository
import com.example.springjpalab.domain.exception.AlreadyExistsException
import com.example.springjpalab.domain.exception.NotFoundException
import com.example.springjpalab.domain.model.Role
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.cache.annotation.CacheEvict
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody


@Service
class AuthService (
    private val userRepository: UserJpaRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {
    private val logger = KotlinLogging.logger {}

    @CacheEvict(cacheNames = ["users"], allEntries = true)
    fun register(@RequestBody @Valid request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw AlreadyExistsException("Пользователь с email ${request.email} уже существует")
        }

        val encodedPassword = requireNotNull(passwordEncoder.encode(request.password)) {
            "Не удалось захешировать пароль"
        }

        val user = UserJpaEntity(
            email = request.email,
            passwordHash = encodedPassword,
            firstName = request.name,
            lastName = request.name,
            role = Role.USER
        )


        val savedUser = userRepository.save(user)
        logger.info { "Зарегистрирован пользователь: ${savedUser.email}" }

        val token = jwtService.generateToken(savedUser.email, savedUser.role.name)
        return AuthResponse(token, savedUser.email, savedUser.role.name)
    }
    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val user = userRepository.findByEmail(request.email)
            ?: throw NotFoundException("Пользователь не найден")

        logger.info { "Вход пользователя: ${user.email}" }

        val token = jwtService.generateToken(user.email, user.role.name)
        return AuthResponse(token, user.email, user.role.name)
    }


}
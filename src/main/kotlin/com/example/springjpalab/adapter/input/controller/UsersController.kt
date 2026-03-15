package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.user.UserCreateRequest
import com.example.springjpalab.adapter.input.dto.user.UserResponse
import com.example.springjpalab.adapter.input.dto.user.UserUpdateRequest
import com.example.springjpalab.application.service.UserService
import com.example.springjpalab.domain.model.User
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping("/api/v1/users")
class UsersController (
    private val userService: UserService
) {
    @GetMapping
    fun getAllUsers(): ResponseEntity<Any> {
        val usersList = userService.findAll()
        return ResponseEntity.ok(usersList)
    }

    @PostMapping
    fun createUser(@Valid @RequestBody request: UserCreateRequest
    ): ResponseEntity<Any> {

        val existingUser = userService.findByEmail(request.email)

        return if (existingUser != null) {
            ResponseEntity.ok(
                UserResponse(
                    id = existingUser.id,
                    email = existingUser.email,
                    firstName = existingUser.firstName,
                    lastName = existingUser.lastName,
                    isActive = existingUser.isActive
                )
            )
        } else {
            val user = User(
                id = 0,
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                isActive = request.isActive
            )
            val saved = userService.create(user)

            ResponseEntity.status(201).body(
                UserResponse(
                    id = saved.id,
                    email = saved.email,
                    firstName = saved.firstName,
                    lastName = saved.lastName,
                    isActive = saved.isActive
                )
            )
        }
    }


    @GetMapping("/{id}")
    fun getUserById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Any> {
        val user = userService.findById(id)
        return ResponseEntity.ok(
            UserResponse(
                id,
                user.email,
                user.firstName,
                user.lastName,
                user.isActive
            )
        )

    }

    @PutMapping("/{id}")
    fun updateUserById(
        @PathVariable(required = true) @Min(1) id: Long,
        @Valid @RequestBody(required = true) request: UserUpdateRequest
    ): ResponseEntity<Any> {
        val existingUser = userService.findById(id)
        val updatedUserEntity = existingUser.copy(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            isActive = request.isActive
        )
        val updatedUser = userService.update(id, updatedUserEntity)

        return ResponseEntity.ok(
            UserResponse(
                updatedUser.id,
                updatedUser.email,
                updatedUser.firstName,
                updatedUser.lastName,
                updatedUser.isActive
            )
        )
    }
    @DeleteMapping("/{id}")
    fun deleteUserById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Any> {
        val existingUser = userService.findById(id)
        userService.delete(existingUser.id)
        return ResponseEntity.noContent().build()

    }

}
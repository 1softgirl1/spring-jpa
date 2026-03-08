package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.error.ErrorResponse
import com.example.springjpalab.adapter.input.dto.user.UserCreateRequest
import com.example.springjpalab.adapter.input.dto.user.UserResponse
import com.example.springjpalab.adapter.input.dto.user.UserUpdateRequest
import com.example.springjpalab.application.service.UserService
import com.example.springjpalab.domain.model.User
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
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
                isActive = request.active
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
    fun getUserById(@PathVariable(required = true) id: Long): ResponseEntity<Any> {
        val user = userService.findById(id)

        if (user != null) {
            return ResponseEntity.ok(UserResponse(id,
                user.email,
                user.firstName,
                user.lastName,
                user.isActive))
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    404,
                    "Not Found",
                    "User with id=${id} not found"
                )
            )
        }

    }

    @PutMapping("/{id}")
    fun updateUserById(
        @PathVariable(required = true) id: Long,
        @Valid @RequestBody(required = true) request: UserUpdateRequest
    ): ResponseEntity<Any> {
        val existingUser = userService.findById(id)
        return if (existingUser != null) {
            val updatedUserEntity = existingUser.copy(
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                isActive = request.active
            )
            val updatedUser = userService.update(id, updatedUserEntity)

            ResponseEntity.ok(
                UserResponse(
                    updatedUser.id,
                    updatedUser.email,
                    updatedUser.firstName,
                    updatedUser.lastName,
                    updatedUser.isActive
                )
            )
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    404,
                    "Not Found",
                    "User with id=${id} not found"
                )
            )

        }
    }
    @DeleteMapping("/{id}")
    fun deleteUserById(@PathVariable(required = true) id: Long): ResponseEntity<Any> {
        val existingUser = userService.findById(id)
        return if (existingUser != null) {
            userService.delete(existingUser.id)
            ResponseEntity.noContent().build()
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    404,
                    "Not Found",
                    "User with id=${id} not found"
                )
            )
        }

    }

}
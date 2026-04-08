package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.user.UserUpdateRequest
import com.example.springjpalab.adapter.input.mapper.toResponse
import com.example.springjpalab.application.service.UserService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getAllUsers(): ResponseEntity<Any> {
        return ResponseEntity.ok(userService.getAllUsers().map { it.toResponse() })
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    fun getUserById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Any> {
        val user = userService.findById(id)
        return ResponseEntity.ok(user.toResponse())

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateUserById(
        @PathVariable(required = true) @Min(1) id: Long,
        @Valid @RequestBody(required = true) request: UserUpdateRequest
    ): ResponseEntity<Any> {
        val updatedUser = userService.updateUserData(
            id = id,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            isActive = request.isActive
        )

        return ResponseEntity.ok(updatedUser.toResponse())
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteUserById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Any> {
        userService.deleteUserById(id)
        return ResponseEntity.noContent().build()

    }

}
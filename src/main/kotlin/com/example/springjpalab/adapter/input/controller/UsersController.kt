package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.error.ErrorResponse
import com.example.springjpalab.adapter.input.dto.error.ValidationErrorResponse
import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantResponse
import com.example.springjpalab.adapter.input.dto.user.UserResponse
import com.example.springjpalab.adapter.input.dto.user.UserUpdateRequest
import com.example.springjpalab.adapter.input.mapper.toResponse
import com.example.springjpalab.application.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Users", description = "Управление пользователями")
@RequestMapping("/api/v1/users")
class UsersController (
    private val userService: UserService
) {
    @Operation(summary = "Получить всех пользователей")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователи найдены",
                content = [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = UserResponse::class))
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Не аутентифицирован (отсутствует или невалидный токен)",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Unauthorized",
                                value = """{
                                    "status": 401,
                                    "message": "Требуется аутентификация",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Доступ запрещён (недостаточно прав)",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Forbidden",
                                value = """{
                                    "status": 403,
                                    "message": "Доступ запрещён",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            ),
        ]
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>>  {
        return ResponseEntity.ok(userService.getAllUsers().map { it.toResponse() })
    }

    @Operation(summary = "Получить пользователя по ID")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователь найден",
                content = [
                    Content(
                        schema = Schema(implementation = UserResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Не аутентифицирован (отсутствует или невалидный токен)",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Unauthorized",
                                value = """{
                                    "status": 401,
                                    "message": "Требуется аутентификация",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Доступ запрещён (недостаточно прав)",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Forbidden",
                                value = """{
                                    "status": 403,
                                    "message": "Доступ запрещён",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Validation parameters error",
                content = [
                    Content(
                        schema = Schema(implementation = ValidationErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Constraint violation",
                                value = """{
                                    "status": 400,
                                    "message": "Constraint violation",
                                    "errors": {
                                        "id": "must be greater than or equal to 1"
                                    },
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Пользователь не найден",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "User not found",
                                value = """{
                                    "status": 404,
                                    "message": "Пользователь с id=1 не найден",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            )
        ]
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    fun getUserById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<UserResponse> {
        val user = userService.findById(id)
        return ResponseEntity.ok(user.toResponse())

    }

    @Operation(summary = "Обновить пользователя")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователь обновлён",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Пользователь не найден",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "User not found",
                                value = """{
                                    "status": 404,
                                    "message": "Пользователь с id=1 не найден",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации данных",
                content = [
                    Content(
                        schema = Schema(implementation = ValidationErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Validation error",
                                value = """{
                                    "status": 400,
                                    "message": "Method parameter validation error",
                                    "errors": {
                                        "email": "Email must be valid",
                                        "firstName": "First name cannot be blank",
                                        "lastName": "First name cannot be blank",
                                        "role": "Role cannot be blank"
                                    },
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Не аутентифицирован (отсутствует или невалидный токен)",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Unauthorized",
                                value = """{
                                    "status": 401,
                                    "message": "Требуется аутентификация",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Доступ запрещён (недостаточно прав)",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Forbidden",
                                value = """{
                                    "status": 403,
                                    "message": "Доступ запрещён",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            )
        ]
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateUserById(
        @PathVariable(required = true) @Min(1) id: Long,
        @Valid @RequestBody(required = true) request: UserUpdateRequest
    ): ResponseEntity<UserResponse> {
        val updatedUser = userService.updateUserData(
            id = id,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            isActive = request.isActive
        )

        return ResponseEntity.ok(updatedUser.toResponse())
    }

    @Operation(summary = "Удалить пользователя")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Пользователь удалён",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Validation parameters error",
                content = [
                    Content(
                        schema = Schema(implementation = ValidationErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Constraint violation",
                                value = """{
                                    "status": 400,
                                    "message": "Constraint violation",
                                    "errors": {
                                        "id": "must be greater than or equal to 1"
                                    },
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Пользователь не найден",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "User not found",
                                value = """{
                                    "status": 404,
                                    "message": "Пользователь с id=1 не найден",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Не аутентифицирован (отсутствует или невалидный токен)",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Unauthorized",
                                value = """{
                                    "status": 401,
                                    "message": "Требуется аутентификация",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Доступ запрещён (недостаточно прав)",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Forbidden",
                                value = """{
                                    "status": 403,
                                    "message": "Доступ запрещён",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            )
        ]
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteUserById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Void> {
        userService.deleteUserById(id)
        return ResponseEntity.noContent().build()

    }

}

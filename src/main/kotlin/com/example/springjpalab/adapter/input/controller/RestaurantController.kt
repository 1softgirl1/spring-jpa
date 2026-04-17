package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.dish.DishCreateRequest
import com.example.springjpalab.adapter.input.dto.dish.DishResponse
import com.example.springjpalab.adapter.input.dto.error.ErrorResponse
import com.example.springjpalab.adapter.input.dto.error.ValidationErrorResponse
import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantCreateRequest
import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantResponse
import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantUpdateRequest
import com.example.springjpalab.adapter.input.mapper.toResponse
import com.example.springjpalab.application.service.DishService
import com.example.springjpalab.application.service.RestaurantService
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@Validated
@Tag(name = "Restaurants", description = "Управление ресторанами")
@RequestMapping("/api/v1/restaurants")
class RestaurantController(
    private val restaurantService: RestaurantService,
    private val dishService: DishService,
) {

    @Operation(summary = "Получить все рестораны")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Рестораны найдены",
                content = [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = RestaurantResponse::class))
                    )
                ]
            )
        ]
    )
    @GetMapping
    fun getAllRestaurants(): ResponseEntity<List<RestaurantResponse>> {
        return ResponseEntity.ok(restaurantService.findAll().map { it.toResponse() })
    }

    @Operation(summary = "Создать ресторан")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Ресторан создан",
                content = [Content(schema = Schema(implementation = RestaurantResponse::class))]
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
                                        "name": "must not be blank",
                                        "address": "must not be blank"
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
            ),
            ApiResponse(
                responseCode = "409",
                description = "Ресторан с этим именем уже существует",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Restaurant already exists",
                                value = """{
                                    "status": 409,
                                    "message": "Ресторан 'La Piazza' уже существует",
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
    @PostMapping
    fun createRestaurant(@Valid @RequestBody request: RestaurantCreateRequest): ResponseEntity<RestaurantResponse> {
        val saved = restaurantService.createRestaurant(
            name = request.name,
            address = request.address
        )
        return ResponseEntity.status(201).body(saved.toResponse())
    }

    @Operation(summary = "Получить ресторан по ID")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Ресторан найден",
                content = [Content(schema = Schema(implementation = RestaurantResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Ресторан не найден",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Restaurant not found",
                                value = """{
                                    "status": 404,
                                    "message": "Ресторан с id=1 не найден",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            )
        ]
    )
    @GetMapping("/{id}")
    fun getRestaurantById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<RestaurantResponse> {
        return ResponseEntity.ok(restaurantService.findById(id).toResponse())
    }

    @Operation(summary = "Обновить ресторан")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Ресторан обновлён",
                content = [Content(schema = Schema(implementation = RestaurantResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Ресторан не найден",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Restaurant not found",
                                value = """{
                                    "status": 404,
                                    "message": "Ресторан с id=1 не найден",
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
                                        "name": "must not be blank",
                                        "address": "must not be blank"
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
        @Valid @RequestBody(required = true) request: RestaurantUpdateRequest
    ): ResponseEntity<RestaurantResponse> {
        val updatedRest = restaurantService.updateRestaurant(
            id = id,
            name = request.name,
            address = request.address
        )

        return ResponseEntity.ok(updatedRest.toResponse())
    }

    @Operation(summary = "Удалить ресторан")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Ресторан удалён",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Ресторан не найден",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Restaurant not found",
                                value = """{
                                    "status": 404,
                                    "message": "Ресторан с id=1 не найден",
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
        restaurantService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Получить блюда ресторана")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Блюда получены",
                content = [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = DishResponse::class))
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Ресторан не найден",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Restaurant not found",
                                value = """{
                                    "status": 404,
                                    "message": "Ресторан с id=1 не найден",
                                    "timestamp": "2026-04-17T10:30:00"
                                }"""
                            )
                        ]
                    )
                ]
            ),

        ]
    )
    @GetMapping("/{id}/dishes")
    fun getDishes(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<List<DishResponse>> {
        return ResponseEntity.ok(restaurantService.getRestaurantDishes(id).map { it.toResponse() })
    }

    @Operation(summary = "Добавить блюдо в ресторан")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Блюдо создано",
                content = [Content(schema = Schema(implementation = DishResponse::class))]
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
                                        "name": "must not be blank",
                                        "description": "must not be blank",
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
            ),
            ApiResponse(
                responseCode = "404",
                description = "Ресторан не найден",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Restaurant not found",
                                value = """{
                                    "status": 404,
                                    "message": "Ресторан с id=1 не найден",
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
    @PostMapping("/{id}/dishes")
    fun addDishToRestaurant(
        @PathVariable(required = true) @Min(1) id: Long,
        @Valid @RequestBody request: DishCreateRequest
    ): ResponseEntity<DishResponse> {
        val saved = dishService.createInRestaurant(
            restaurantId = id,
            name = request.name,
            description = request.description,
            price = BigDecimal.valueOf(request.price.toDouble()),
            isAvailable = request.isAvailable
        )

        return ResponseEntity.status(201).body(saved.toResponse())
    }
}


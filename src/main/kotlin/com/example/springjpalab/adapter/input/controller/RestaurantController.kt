package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.dish.DishCreateRequest
import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantCreateRequest
import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantUpdateRequest
import com.example.springjpalab.adapter.input.mapper.toResponse
import com.example.springjpalab.application.service.DishService
import com.example.springjpalab.application.service.RestaurantService
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
@RequestMapping("/api/v1/restaurants")
class RestaurantController(
    private val restaurantService: RestaurantService,
    private val dishService: DishService,
) {
    @GetMapping
    fun getAllRestaurants(): ResponseEntity<Any> {
        return ResponseEntity.ok(restaurantService.findAll().map { it.toResponse() })
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun createRestaurant(@Valid @RequestBody request: RestaurantCreateRequest): ResponseEntity<Any>
   {
        val saved = restaurantService.createRestaurant(
            name = request.name,
            address = request.address
        )
        return ResponseEntity.status(201).body(saved.toResponse())
    }


    @GetMapping("/{id}")
    fun getRestaurantById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(restaurantService.findById(id).toResponse())
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateUserById(
        @PathVariable(required = true) @Min(1) id: Long,
        @Valid @RequestBody(required = true) request: RestaurantUpdateRequest
    ): ResponseEntity<Any> {
        val updatedRest = restaurantService.updateRestaurant(
            id = id,
            name = request.name,
            address = request.address
        )

        return ResponseEntity.ok(updatedRest.toResponse())
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteUserById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Any> {
        restaurantService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/dishes")
    fun getDishes(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(restaurantService.getRestaurantDishes(id).map { it.toResponse() })
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/dishes")
    fun addDishToRestaurant(
        @PathVariable(required = true) @Min(1) id: Long,
        @Valid @RequestBody request: DishCreateRequest
    ): ResponseEntity<Any> {
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
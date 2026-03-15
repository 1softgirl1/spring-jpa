package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.dish.DishCreateRequest
import com.example.springjpalab.adapter.input.dto.dish.DishResponse
import com.example.springjpalab.adapter.input.dto.dish.DishUpdateRequest
import com.example.springjpalab.application.service.DishService
import com.example.springjpalab.application.service.RestaurantService
import com.example.springjpalab.domain.model.Dish
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal


@RestController
@RequestMapping("/api/v1/dishes")
@Validated
class DishesController(
    private val dishService: DishService,
    private val restaurantService: RestaurantService
) {
    @GetMapping
    fun getDishesByNamePart(
        @RequestParam(required = false) @Size(min = 2, message = "Минимум 2 символа для поиска") namePart: String?
    ): ResponseEntity<Any> {

        return if (namePart.isNullOrBlank()) {
            ResponseEntity.ok(dishService.findAll())
        } else {
            ResponseEntity.ok(dishService.findByNamePart(namePart))
        }

    }

    @GetMapping("/{id}")
    fun getDishById(
        @PathVariable(required = true) @Min(1)  id: Long): ResponseEntity<Any> {
        val dish = dishService.findById(id)
        return ResponseEntity.ok(
            DishResponse(
                id = dish.id,
                name = dish.name,
                description = dish.description,
                price = dish.price,
                isAvailable = dish.isAvailable,
                restaurantId = dish.restaurantId
            )
        )
    }

    @PostMapping("/restaurants/{restaurantId}/dishes")
    fun createDishInRestaurant(
        @PathVariable(required = true) @Min(1) restaurantId: Long,
        @Valid @RequestBody request: DishCreateRequest
    ): ResponseEntity<DishResponse> {
        restaurantService.findById(restaurantId)
        val dish = Dish(
            id = 0,
            name = request.name,
            description = request.description,
            price = BigDecimal.valueOf(request.price.toDouble()),
            isAvailable = request.isAvailable,
            restaurantId = restaurantId
        )
        val saved = dishService.create(dish)
        return ResponseEntity.status(201).body(
            DishResponse(
                id = saved.id,
                name = saved.name,
                description = saved.description,
                price = saved.price,
                isAvailable = saved.isAvailable,
                restaurantId = saved.restaurantId
            )
        )
    }

    @PostMapping("/api/v1/restaurants/{restaurantId}/dishes")
    fun createDishInRestaurantV1(
        @PathVariable(required = true) @Min(1) restaurantId: Long,
        @Valid @RequestBody request: DishCreateRequest
    ): ResponseEntity<DishResponse> {
        restaurantService.findById(restaurantId)
        val dish = Dish(
            id = 0,
            name = request.name,
            description = request.description,
            price = BigDecimal.valueOf(request.price.toDouble()),
            isAvailable = request.isAvailable,
            restaurantId = restaurantId
        )
        val saved = dishService.create(dish)
        return ResponseEntity.status(201).body(
            DishResponse(
                id = saved.id,
                name = saved.name,
                description = saved.description,
                price = saved.price,
                isAvailable = saved.isAvailable,
                restaurantId = saved.restaurantId
            )
        )
    }


    @PutMapping("/{id}")
    fun updateDishById(
        @PathVariable(required = true) @Min(1) id: Long,
        @Valid @RequestBody(required = true) request: DishUpdateRequest
    ): ResponseEntity<Any> {
        val existingDish = dishService.findById(id)
        val updatedDishEntity = existingDish.copy(
            name = request.name,
            description = request.description,
            price = BigDecimal.valueOf(request.price.toDouble()),
            isAvailable = request.isAvailable
        )
        val updatedDish = dishService.update(id, updatedDishEntity)

        return ResponseEntity.ok(
            DishResponse(
                id = updatedDish.id,
                name = updatedDish.name,
                description = updatedDish.description,
                price = updatedDish.price,
                isAvailable = updatedDish.isAvailable,
                restaurantId = updatedDish.restaurantId
            )
        )
    }
    @DeleteMapping("/{id}")
    fun deleteDishById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Any> {
        val existingDish = dishService.findById(id)
        dishService.delete(existingDish.id)
        return ResponseEntity.noContent().build()

    }


}
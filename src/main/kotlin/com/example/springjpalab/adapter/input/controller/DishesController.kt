package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.dish.DishCreateRequest
import com.example.springjpalab.adapter.input.dto.dish.DishResponse
import com.example.springjpalab.adapter.input.dto.error.ErrorResponse

import com.example.springjpalab.adapter.input.dto.dish.DishUpdateRequest

import com.example.springjpalab.application.service.DishService

import com.example.springjpalab.domain.model.Dish
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
class DishesController(
    private val dishService: DishService
) {
    @GetMapping
    fun getDishesByNamePart(
        @RequestParam(required = false) namePart: String?
    ): ResponseEntity<Any> {

        return if (namePart.isNullOrBlank()) {
            ResponseEntity.ok(dishService.findAll())
        } else {
            ResponseEntity.ok(dishService.findByNamePart(namePart))
        }

    }

    @GetMapping("/{id}")
    fun getDishById(@PathVariable(required = true) id: Long): ResponseEntity<Any> {
        val dish = dishService.findById(id)

        if (dish != null) {
            return ResponseEntity.ok(DishResponse(id,
                dish.name,
                dish.description,
                dish.price,
                dish.isAvailable,
                dish.restaurantId))
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    404,
                    "Not Found",
                    "Dish with id=${id} not found"
                )
            )
        }

    }


    @PutMapping("/{id}")
    fun updateDishById(
        @PathVariable(required = true) id: Long,
        @Valid @RequestBody(required = true) request: DishUpdateRequest
    ): ResponseEntity<Any> {
        val existingDish = dishService.findById(id)
        return if (existingDish != null) {
            val updatedDishEntity = existingDish.copy(
                name = request.name,
                description = request.description,
                price = BigDecimal.valueOf(request.price.toDouble()),
                isAvailable = request.isAvailable
            )
            val updatedDish = dishService.update(id, updatedDishEntity)

            ResponseEntity.ok(
                DishResponse(
                    updatedDish.id,
                    updatedDish.name,
                    updatedDish.description,
                    updatedDish.price,
                    updatedDish.isAvailable,
                    updatedDish.restaurantId
                )
            )
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    404,
                    "Not Found",
                    "Dish with id=${id} not found"
                )
            )

        }
    }
    @DeleteMapping("/{id}")
    fun deleteDishById(@PathVariable(required = true) id: Long): ResponseEntity<Any> {
        val existingDish = dishService.findById(id)
        return if (existingDish != null) {
            dishService.delete(existingDish.id)
            ResponseEntity.noContent().build()
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    404,
                    "Not Found",
                    "Dish with id=${id} not found"
                )
            )
        }

    }


}
package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.dish.DishCreateRequest
import com.example.springjpalab.adapter.input.dto.dish.DishUpdateRequest
import com.example.springjpalab.adapter.input.mapper.toResponse
import com.example.springjpalab.application.service.DishService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal


@RestController
@RequestMapping("/api/v1/dishes")
@Validated
class DishesController(
    private val dishService: DishService
) {
    @GetMapping
    fun getDishesByNamePart(
        @RequestParam(required = false) @Size(min = 2, message = "Минимум 2 символа для поиска") namePart: String?
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(dishService.getDishes(namePart).map { it.toResponse() })
    }

    @GetMapping("/{id}")
    fun getDishById(
        @PathVariable(required = true) @Min(1)  id: Long): ResponseEntity<Any> {
        val dish = dishService.findById(id)
        return ResponseEntity.ok(dish.toResponse())
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateDishById(
        @PathVariable(required = true) @Min(1) id: Long,
        @Valid @RequestBody(required = true) request: DishUpdateRequest
    ): ResponseEntity<Any> {
        val updatedDish = dishService.updateDish(
            id = id,
            name = request.name,
            description = request.description,
            price = BigDecimal.valueOf(request.price.toDouble()),
            isAvailable = request.isAvailable
        )

        return ResponseEntity.ok(updatedDish.toResponse())
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteDishById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Any> {
        dishService.delete(id)
        return ResponseEntity.noContent().build()

    }


}
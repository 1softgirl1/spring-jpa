package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.dish.DishCreateRequest
import com.example.springjpalab.adapter.input.dto.dish.DishResponse
import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantCreateRequest
import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantResponse
import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantUpdateRequest
import com.example.springjpalab.application.service.DishService
import com.example.springjpalab.application.service.RestaurantService
import com.example.springjpalab.domain.model.Dish
import com.example.springjpalab.domain.model.Restaurant
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
import java.math.BigDecimal
import kotlin.text.toDouble

@RestController
@Validated
@RequestMapping("/api/v1/restaurants")
class RestaurantController(
    private val restaurantService: RestaurantService,
    private val dishService: DishService,
) {
    @GetMapping
    fun getAllRestaurants(): ResponseEntity<Any> {
        val restList = restaurantService.findAll()
        return ResponseEntity.ok(
            restList.map { r ->
                RestaurantResponse(
                    id = r.id,
                    name = r.name,
                    address = r.address
                )
            }
        )
    }

    @PostMapping
    fun createRestaurant(@Valid @RequestBody request: RestaurantCreateRequest): ResponseEntity<Any>
   {
        val rest = Restaurant(
            id = 0,
            name = request.name,
            address = request.address,
            dishes = emptyList(),
        )
        val saved = restaurantService.create(rest)

        return ResponseEntity.status(201).body(
            RestaurantResponse(
                id = saved.id,
                name = saved.name,
                address = saved.address
            )
        )
    }


    @GetMapping("/{id}")
    fun getRestaurantById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Any> {
        val rest = requireNotNull(restaurantService.findById(id))
        return ResponseEntity.ok(
            RestaurantResponse(
                id = rest.id,
                name = rest.name,
                address = rest.address
            )
        )
    }

    @PutMapping("/{id}")
    fun updateUserById(
        @PathVariable(required = true) @Min(1) id: Long,
        @Valid @RequestBody(required = true) request: RestaurantUpdateRequest
    ): ResponseEntity<Any> {
        val existingRest = requireNotNull(restaurantService.findById(id))
        val updatedRestEntity = existingRest.copy(
            name = request.name,
            address = request.address
        )
        val updatedRest = restaurantService.update(id, updatedRestEntity)

        return ResponseEntity.ok(
            RestaurantResponse(
                id = updatedRest.id,
                name = updatedRest.name,
                address = updatedRest.address
            )
        )
    }

    @DeleteMapping("/{id}")
    fun deleteUserById(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Any> {
        val existingRest = requireNotNull(restaurantService.findById(id))
        restaurantService.delete(existingRest.id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/dishes")
    fun getDishes(@PathVariable(required = true) @Min(1) id: Long): ResponseEntity<Any> {
        val restaurant = restaurantService.findById(id)!!

        val dishesResponse = restaurant.dishes.map { dish ->
            DishResponse(
                id = dish.id,
                name = dish.name,
                description = dish.description,
                price = dish.price,
                isAvailable = dish.isAvailable,
                restaurantId = restaurant.id
            )
        }

        return ResponseEntity.ok(dishesResponse)
    }
    @PostMapping("/{restaurantId}/dishes")
    fun addDishToRestaurant(
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








}
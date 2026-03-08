package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.dish.DishCreateRequest
import com.example.springjpalab.adapter.input.dto.dish.DishResponse
import com.example.springjpalab.adapter.input.dto.error.ErrorResponse
import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantCreateRequest
import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantResponse
import com.example.springjpalab.adapter.input.dto.restaurant.RestaurantUpdateRequest
import com.example.springjpalab.adapter.input.dto.user.UserCreateRequest
import com.example.springjpalab.adapter.input.dto.user.UserResponse
import com.example.springjpalab.adapter.input.dto.user.UserUpdateRequest
import com.example.springjpalab.application.service.DishService
import com.example.springjpalab.application.service.RestaurantService
import com.example.springjpalab.application.service.UserService
import com.example.springjpalab.domain.model.Dish
import com.example.springjpalab.domain.model.Restaurant
import com.example.springjpalab.domain.model.User
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
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/api/v1/restaurants")
class RestaurantController(
    private val restaurantService: RestaurantService,
    private val dishService: DishService,
) {
    @GetMapping
    fun getAllRestaurants(): ResponseEntity<Any> {
        val restList = restaurantService.findAll()
        return ResponseEntity.ok(restList)
    }

    @PostMapping
    fun createRestaurant(@Valid @RequestBody request: RestaurantCreateRequest): ResponseEntity<Any>
   {

        val existingRestaurant = restaurantService.findByName(request.name)

        return if (existingRestaurant != null) {
            ResponseEntity.ok(
                RestaurantResponse(
                    id = existingRestaurant.id,
                    name = existingRestaurant.name,
                    address = existingRestaurant.address

                )
            )
        } else {
            val rest = Restaurant(
                id = 0,
                name = request.name,
                address = request.address,
                dishes = emptyList(),
            )
            val saved = restaurantService.create(rest)

            ResponseEntity.status(201).body(
                RestaurantResponse(
                    id = saved.id,
                    name = saved.name,
                    address = saved.address
                )
            )
        }
    }


    @GetMapping("/{id}")
    fun getRestaurantById(@PathVariable(required = true) id: Long): ResponseEntity<Any> {
        val rest = restaurantService.findById(id)

        if (rest != null) {
            return ResponseEntity.ok(RestaurantResponse(id,
                rest.name,
                rest.address))
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    404,
                    "Not Found",
                    "Restaurant with id=${id} not found"
                )
            )
        }

    }

    @PutMapping("/{id}")
    fun updateUserById(
        @PathVariable(required = true) id: Long,
        @Valid @RequestBody(required = true) request: RestaurantUpdateRequest
    ): ResponseEntity<Any> {
        val existingRest = restaurantService.findById(id)
        return if (existingRest != null) {
            val updatedRestEntity = existingRest.copy(
                name = request.name,
                address = request.address
            )
            val updatedRest = restaurantService.update(id, updatedRestEntity)

            ResponseEntity.ok(
                RestaurantResponse(
                    updatedRest.id,
                    updatedRest.name,
                    updatedRest.address
                )
            )
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    404,
                    "Not Found",
                    "Restaurant with id=${id} not found"
                )
            )

        }
    }

    @DeleteMapping("/{id}")
    fun deleteUserById(@PathVariable(required = true) id: Long): ResponseEntity<Any> {
        val existingRest = restaurantService.findById(id)
        return if (existingRest != null) {
            restaurantService.delete(existingRest.id)
            ResponseEntity.noContent().build()
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    404,
                    "Not Found",
                    "Restaurant with id=${id} not found"
                )
            )
        }
    }

    @GetMapping("/{id}/dishes")
    fun getDishes(@PathVariable id: Long): ResponseEntity<Any> {
        val restaurant = restaurantService.findById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    status = 404,
                    error = "Not Found",
                    message = "Restaurant with id=$id not found"
                )
            )

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

    @PostMapping("/{id}/dishes")
    fun addDishToRestaurant(
        @PathVariable id: Long,
        @Valid @RequestBody request: DishCreateRequest
    ): ResponseEntity<Any> {
        val restaurant = restaurantService.findById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    status = 404,
                    error = "Not Found",
                    message = "Restaurant with id=$id not found"
                )
            )

        val dish = Dish(
            id = 0,
            name = request.name,
            description = request.description,
            price = BigDecimal.valueOf(request.price.toDouble()),
            isAvailable = request.isAvailable,
            restaurantId = restaurant.id
        )

        val createdDish = dishService.create(dish)

        return ResponseEntity.status(HttpStatus.CREATED).body(createdDish)
    }




}
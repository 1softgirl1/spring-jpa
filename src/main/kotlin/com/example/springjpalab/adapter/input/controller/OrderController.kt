package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.dish.DishResponse
import com.example.springjpalab.adapter.input.dto.error.ErrorResponse
import com.example.springjpalab.adapter.input.dto.order.OrderCreateRequest
import com.example.springjpalab.adapter.input.dto.order.OrderResponse
import com.example.springjpalab.adapter.input.dto.order.OrderStatusUpdateRequest
import com.example.springjpalab.adapter.output.jpa.entity.OrderStatus
import com.example.springjpalab.application.service.DishService
import com.example.springjpalab.application.service.OrderService
import com.example.springjpalab.application.service.RestaurantService
import com.example.springjpalab.application.service.UserService
import com.example.springjpalab.domain.model.Order
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/v1/orders")
class OrderController (
    private val orderService: OrderService,
    private val userService: UserService,
    private val dishService: DishService, private val restaurantService: RestaurantService
){
    @GetMapping
    fun getOrders(@RequestParam(required = false) userId: Long?,
                  @RequestParam(required = false) status: OrderStatus?
    ): ResponseEntity<Any> {
        val orders = when {
            userId != null && status != null ->
                orderService.findByUserIdAndStatus(userId, status)
            userId != null ->
                orderService.findByUserId(userId)
            status != null ->
                orderService.findByStatus(status)
            else ->
                orderService.findAll()
        }
        if (orders == null) {
            return ResponseEntity.ok(emptyList<OrderResponse>())
        }

        return ResponseEntity.ok(
            orders.map { order ->
                OrderResponse(
                    id = order.id,
                    status = order.status,
                    createdAt = order.createdAt.toString(),
                    userId = order.userId,
                    dishes = order.dishes.map { dish ->
                        DishResponse(
                            id = dish.id,
                            name = dish.name,
                            description = dish.description,
                            price = dish.price,
                            isAvailable = dish.isAvailable,
                            restaurantId = dish.restaurantId
                        )
                    }
                )
            }
        )


    }
    @PostMapping
    fun createOrder(@Valid @RequestBody request: OrderCreateRequest): ResponseEntity<Any>{
        val user = userService.findById(request.userId)
            ?: return ResponseEntity.status(400).body(
                ErrorResponse(400, "Validation error", "User not found with id ${request.userId}")
            )


        val dishes = request.dishIds.mapNotNull { dishId ->
            dishService.findById(dishId)
        }
        if (dishes.size != request.dishIds.size) {
            return ResponseEntity.status(400).body(
                ErrorResponse(400, "Validation error", "Some dishIds not found")
            )
        }

        val order = Order(
            id = 0,
            status = OrderStatus.PENDING,
            createdAt = LocalDateTime.now(),
            userId = request.userId,
            dishes = dishes,
        )
        val saved = orderService.create(order)

        return ResponseEntity.status(201).body(
            OrderResponse(
                id = saved.id,
                status = saved.status,
                createdAt = saved.createdAt.toString(),
                userId = saved.userId,
                dishes = saved.dishes.map { dish ->
                    DishResponse(
                        id = dish.id,
                        name = dish.name,
                        description = dish.description,
                        price = dish.price,
                        isAvailable = dish.isAvailable,
                        restaurantId = dish.restaurantId
                    )
                }
            )
        )

    }
    @GetMapping("/{id}")
    fun getOrderById(@PathVariable id: Long): ResponseEntity<Any>{
        val order = orderService.findById(id)
        if (order != null) {
            return ResponseEntity.ok(OrderResponse(
                id = order.id,
                status = order.status,
                createdAt = order.createdAt.toString(),
                userId = order.userId,
                dishes = order.dishes.map { dish ->
                    DishResponse(
                        id = dish.id,
                        name = dish.name,
                        description = dish.description,
                        price = dish.price,
                        isAvailable = dish.isAvailable,
                        restaurantId = dish.restaurantId
                    )
                }
            ))
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    404,
                    "Not Found",
                    "Order with id=${id} not found"
                )
            )
        }
    }
    @PatchMapping("/{id}/status")
    fun updateOrderStatus(@PathVariable id: Long, @Valid @RequestBody request: OrderStatusUpdateRequest) : ResponseEntity<Any>{
        val order = orderService.findById(id)
        return if (order != null) {
            val updatedOrderEntity = order.copy(
                status = request.status

            )
            val updatedOrder = orderService.update(id, updatedOrderEntity)

            ResponseEntity.ok(
                OrderResponse(
                    id = updatedOrder.id,
                    status = updatedOrder.status,
                    createdAt = updatedOrder.createdAt.toString(),
                    userId = updatedOrder.userId,
                    dishes = updatedOrder.dishes.map { dish ->
                        DishResponse(
                            id = dish.id,
                            name = dish.name,
                            description = dish.description,
                            price = dish.price,
                            isAvailable = dish.isAvailable,
                            restaurantId = dish.restaurantId
                        )
                    }
                )
            )
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    404,
                    "Not Found",
                    "Order with id=${id} not found"
                )
            )

        }

    }


}
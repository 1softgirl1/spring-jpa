package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.dish.DishResponse
import com.example.springjpalab.adapter.input.dto.order.OrderCreateRequest
import com.example.springjpalab.adapter.input.dto.order.OrderResponse
import com.example.springjpalab.adapter.input.dto.order.OrderStatusUpdateRequest
import com.example.springjpalab.adapter.output.jpa.entity.OrderStatus
import com.example.springjpalab.application.service.DishService
import com.example.springjpalab.application.service.OrderService
import com.example.springjpalab.application.service.UserService
import com.example.springjpalab.domain.model.Order
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
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
@Validated
class OrderController (
    private val orderService: OrderService,
    private val userService: UserService,
    private val dishService: DishService
){
    @GetMapping
    fun getOrders(@RequestParam(required = false) @Min(1) userId: Long?,
                  @RequestParam(required = false) status: OrderStatus?
    ): ResponseEntity<Any> {
        val ordersNullable = when {
            userId != null && status != null ->
                orderService.findByUserIdAndStatus(userId, status)
            userId != null ->
                orderService.findByUserId(userId)
            status != null ->
                orderService.findByStatus(status)
            else ->
                orderService.findAll()
        }
        val orders = ordersNullable ?: emptyList()

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
        if (request.dishIds.isEmpty()) {
            throw IllegalArgumentException("dishIds cannot be empty")
        }

        userService.findById(request.userId)

        val dishes = request.dishIds.map { dishId ->
            dishService.findById(dishId)
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
    fun getOrderById(@PathVariable @Min(1) id: Long): ResponseEntity<Any>{
        val order = requireNotNull(orderService.findById(id))
        return ResponseEntity.ok(
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
        )
    }
    @PatchMapping("/{id}/status")
    fun updateOrderStatus(
        @PathVariable(required = true) @Min(1) id: Long,
        @Valid @RequestBody request: OrderStatusUpdateRequest
    ): ResponseEntity<Any> {
        val order = requireNotNull(orderService.findById(id))
        val updatedOrder = orderService.update(
            id,
            order.copy(status = request.status)
        )

        return ResponseEntity.ok(
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


}
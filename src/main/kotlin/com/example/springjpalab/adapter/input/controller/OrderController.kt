package com.example.springjpalab.adapter.input.controller

import com.example.springjpalab.adapter.input.dto.order.OrderCreateRequest
import com.example.springjpalab.adapter.input.dto.order.OrderStatusUpdateRequest
import com.example.springjpalab.adapter.input.mapper.toResponse
import com.example.springjpalab.domain.model.OrderStatus
import com.example.springjpalab.application.service.OrderService
import com.example.springjpalab.application.service.UserService
import com.example.springjpalab.domain.model.User
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/orders")
@Validated
class OrderController (
    private val orderService: OrderService,
    private val userService: UserService,
){
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    fun getOrders(
        @RequestParam(required = false) @Min(1) userId: Long?,
        @RequestParam(required = false) status: OrderStatus?,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Any> {
        val requester = currentUser(userDetails)
        val orders = orderService.getOrdersForRequester(
            requestedUserId = userId,
            status = status,
            requesterId = requester.id,
            isAdmin = isAdmin(userDetails)
        )
        return ResponseEntity.ok(orders.map { it.toResponse() })
    }
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    fun createOrder(
        @Valid @RequestBody request: OrderCreateRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Any>{
        val saved = orderService.createForUser(
            userId = currentUser(userDetails).id,
            dishIds = request.dishIds
        )
        return ResponseEntity.status(201).body(saved.toResponse())
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    fun getOrderById(
        @PathVariable @Min(1) id: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Any>{
        val order = orderService.getByIdForRequester(
            orderId = id,
            requesterId = currentUser(userDetails).id,
            isAdmin = isAdmin(userDetails)
        )
        return ResponseEntity.ok(order.toResponse())
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    fun updateOrderStatus(
        @PathVariable(required = true) @Min(1) id: Long,
        @Valid @RequestBody request: OrderStatusUpdateRequest
    ): ResponseEntity<Any> {
        val order = orderService.findById(id)
        val updatedOrder = orderService.update(
            id,
            order.copy(status = request.status)
        )
        return ResponseEntity.ok(updatedOrder.toResponse())
    }

    private fun currentUser(userDetails: UserDetails): User =
        userService.findByEmail(userDetails.username)
            ?: throw IllegalArgumentException("Пользователь не найден")

    private fun isAdmin(userDetails: UserDetails): Boolean =
        userDetails.authorities.any { it.authority == "ROLE_ADMIN" }


}

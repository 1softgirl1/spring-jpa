package com.example.springjpalab.adapter.input.dto.order

import com.example.springjpalab.adapter.output.jpa.entity.OrderStatus
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


class OrderStatusUpdateRequest() {

    @field:NotNull(message = "status cannot be null")
    lateinit var status: OrderStatus
}
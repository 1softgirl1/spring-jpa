package com.example.springjpalab.adapter.input.dto.dish

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size


data class DishUpdateRequest(
    @field:NotBlank(message = "Name cannot be blank")
    @field:Size(min = 1, message = "Name must be at least 1 character")
    val name: String,

    @field:NotBlank(message = "Description cannot be blank")
    @field:Size(min = 1, message = "Description must be at least 1 character")
    val description: String,

    @field:NotNull(message = "Price cannot be null")
    @field:DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @field:DecimalMax(value = "499.0", message = "Price must be less than 499")
    val price: Number,

    @field:NotNull(message = "Availability must be specified")
    @field:JsonProperty("isAvailable")
    @get:JsonIgnore
    val isAvailable: Boolean
)

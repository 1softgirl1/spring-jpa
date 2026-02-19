package com.example.springjpalab.adapter.input.dto.dish

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class DishResponse(

    @field:NotNull(message = "Id cannot be null")
    val id: Long,

    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:NotBlank(message = "Description cannot be blank")
    val description: String,

    @field:NotNull(message = "Price cannot be null")
    val price: Number,

    @field:NotNull(message = "Availability must be specified")
    @field:JsonProperty("isAvailable")
    @get:JsonIgnore
    val isAvailable: Boolean
)

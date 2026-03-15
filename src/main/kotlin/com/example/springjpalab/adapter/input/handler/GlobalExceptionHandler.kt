package com.example.springjpalab.adapter.input.handler

import com.example.springjpalab.adapter.input.dto.error.ErrorResponse
import com.example.springjpalab.adapter.input.dto.error.ValidationErrorResponse
import com.example.springjpalab.domain.exception.AlreadyExistsException
import com.example.springjpalab.domain.exception.AppException
import com.example.springjpalab.domain.exception.InvalidOrderStateException
import com.example.springjpalab.domain.exception.NotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(AppException::class)
    fun handleCommon(
        e: AppException
    ): ResponseEntity<ErrorResponse> {
        val httpStatus = when (e) {
            is NotFoundException -> HttpStatus.NOT_FOUND
            is AlreadyExistsException -> HttpStatus.CONFLICT
            is InvalidOrderStateException -> HttpStatus.BAD_REQUEST
        }

        when (e) {
            is NotFoundException -> logger.warn { e.message }
            else -> logger.warn { e.message }
        }

        return ResponseEntity
            .status(httpStatus)
            .body(
                ErrorResponse(
                    status = httpStatus.value(),
                    message = e.message
                )
            )
    }


    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        logger.warn { "MethodArgumentNotValidException: ${ex.message}" }
        val errors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Incorrect value")
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Method parameter validation error",
                errors
            ))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warn { "IllegalArgumentException: ${e.message}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = e.message
                )
            )
    }


    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error(e) { "Непредвиденная ошибка" }
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(500, "Внутренняя ошибка сервера"))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(e: ConstraintViolationException): ResponseEntity<ValidationErrorResponse> {
        logger.warn { "ConstraintViolationException: ${e.message}" }
        val errors = e.constraintViolations
            .associate { violation ->
                val field = violation.propertyPath?.toString()?.substringAfterLast('.') ?: "value"
                field to (violation.message ?: "Incorrect value")
            }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ValidationErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = "Constraint violation",
                    errors = errors
                )
            )
    }

}
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
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.regex.Pattern
import org.springframework.security.access.AccessDeniedException


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

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        logger.warn(e) { "HttpRequestMethodNotSupportedException" }
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(
                ErrorResponse(
                    status = HttpStatus.METHOD_NOT_ALLOWED.value(),
                    message = "Метод не поддерживается"
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
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        logger.warn(e) { "HttpMessageNotReadableException (invalid request body)" }

        val fieldName = extractMissingFieldName(e)
        if (fieldName != null) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                    ValidationErrorResponse(
                        status = HttpStatus.BAD_REQUEST.value(),
                        message = "Validation error",
                        errors = mapOf(fieldName to "Field is required")
                    )
                )
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = "Malformed JSON request"
                )
            )
    }

    private fun extractMissingFieldName(e: HttpMessageNotReadableException): String? {
        val patterns = listOf(
            Pattern.compile("Missing required creator property '([^']+)'")
                to 1,
            Pattern.compile("creator parameter ([A-Za-z0-9_]+)")
                to 1,
            // Kotlin/Jackson: "Parameter specified as non-null is null: ... parameter firstName"
            Pattern.compile("parameter\\s+([A-Za-z0-9_]+)")
                to 1,
        )

        var current: Throwable? = e
        while (current != null) {
            val messages = listOfNotNull(current.message, current.localizedMessage)
            for (msg in messages) {
                for ((pattern, groupIdx) in patterns) {
                    val matcher = pattern.matcher(msg)
                    if (matcher.find()) {
                        return matcher.group(groupIdx)
                    }
                }
            }
            current = current.cause
        }

        return null
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(e: BadCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(401, "Неверный email или пароль"))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(403, "Доступ запрещён"))
    }

}
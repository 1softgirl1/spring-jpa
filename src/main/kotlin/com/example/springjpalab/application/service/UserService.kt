package com.example.springjpalab.application.service

import com.example.springjpalab.domain.exception.AlreadyExistsException
import com.example.springjpalab.domain.exception.NotFoundException
import com.example.springjpalab.domain.port.UserRepositoryPort
import com.example.springjpalab.domain.model.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repository: UserRepositoryPort
) {

    private val logger = KotlinLogging.logger {}

    fun create(user: User): User {
        val existing = repository.findByEmail(user.email)
        if (existing != null) {
            logger.warn { "Пользователь с email=${user.email} уже существует" }
            throw AlreadyExistsException("Пользователь с email=${user.email} уже существует")
        }

        val newUser = user.copy(id = 0)
        val saved = repository.create(newUser)
        logger.info { "Создан пользователь: id=${saved.id}, email=${saved.email}" }
        return saved
    }

    fun update(id: Long, user: User): User {
        repository.findById(id) ?: run {
            logger.warn { "Пользователь с id=$id не найден" }
            throw NotFoundException("Пользователь с id=$id не найден")
        }

        val updated = user.copy(id = id)
        val saved = repository.update(updated)
        logger.info { "Обновлен пользователь: id=${saved.id}, email=${saved.email}" }
        return saved
    }

    fun findById(id: Long): User =
        repository.findById(id) ?: run {
            logger.warn { "Пользователь с id=$id не найден" }
            throw NotFoundException("Пользователь с id=$id не найден")
        }

    fun findByEmail(email: String): User? =
        repository.findByEmail(email)

    fun findAll(): List<User> =
        repository.findAll()

    fun getAllUsers(): List<User> =
        findAll()

    fun updateUserData(
        id: Long,
        email: String,
        firstName: String,
        lastName: String,
        isActive: Boolean
    ): User {
        val existing = findById(id)
        return update(
            id,
            existing.copy(
                email = email,
                firstName = firstName,
                lastName = lastName,
                isActive = isActive
            )
        )
    }

    fun delete(id: Long) {
        repository.findById(id) ?: run {
            logger.warn { "Пользователь с id=$id не найден" }
            throw NotFoundException("Пользователь с id=$id не найден")
        }
        repository.deleteById(id)
        logger.info { "Удален пользователь: id=$id" }
    }

    fun deleteUserById(id: Long) {
        delete(id)
    }
}

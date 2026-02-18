package com.example.springjpalab.application.service

import com.example.springjpalab.domain.port.UserRepositoryPort
import com.example.springjpalab.domain.model.User
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repository: UserRepositoryPort
) {

    fun create(user: User): User {

        val newUser = user.copy(id = 0)

        return repository.create(newUser)
    }

    fun update(id: Long, user: User): User {

        val updated = user.copy(id = id)

        return repository.update(updated)
    }

    fun findById(id: Long): User? =
        repository.findById(id)

    fun findByEmail(email: String): User? =
        repository.findByEmail(email)

    fun findAll(): List<User> =
        repository.findAll()

    fun delete(id: Long) =
        repository.deleteById(id)
}

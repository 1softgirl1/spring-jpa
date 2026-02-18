package com.example.springjpalab.domain.port

import com.example.springjpalab.domain.model.User

interface UserRepositoryPort {
    fun findAll(): List<User>
    fun findById(id: Long): User?
    fun findByEmail(email: String): User?
    fun deleteById(id: Long)
    fun create(user: User): User
    fun update(user: User): User
}
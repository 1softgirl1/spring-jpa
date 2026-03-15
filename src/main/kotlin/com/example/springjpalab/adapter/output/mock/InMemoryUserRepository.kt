package com.example.springjpalab.adapter.output.mock

import com.example.springjpalab.domain.port.UserRepositoryPort
import com.example.springjpalab.domain.model.User
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("mock")
class InMemoryUserRepository : UserRepositoryPort {

    private val users = mutableMapOf<Long, User>()
    private var seq = 1L

    override fun create(user: User): User {
        val saved = user.copy(id = seq++)
        users[saved.id] = saved
        return saved
    }

    override fun findAll(): List<User> =
        users.values.toList()

    override fun findById(id: Long) : User? {
        return users[id]
    }

    override fun findByEmail(email: String): User? {
        return users.values.find { it.email == email }
    }


    override fun deleteById(id: Long) {
        users.remove(id)
    }
    override fun update(user: User): User {
        users[user.id] = user
        return user
    }
}


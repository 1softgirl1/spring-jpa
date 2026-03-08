package com.example.springjpalab.adapter.output.jpa.adapter

import com.example.springjpalab.adapter.output.jpa.entity.UserJpaEntity
import com.example.springjpalab.adapter.output.jpa.repository.UserJpaRepository
import com.example.springjpalab.domain.model.User
import com.example.springjpalab.domain.port.UserRepositoryPort
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component


@Component
@Profile("db")
class UserJpaAdapter(
    private val repository: UserJpaRepository
) : UserRepositoryPort {

    override fun create(user: User): User {
        val entity = UserJpaEntity(
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            isActive = user.isActive
        )
        val saved = repository.save(entity)
        return saved.toDomain()
    }

    override fun update(user: User): User {
        val existing = repository.findById(user.id ?: throw IllegalArgumentException("User id is null"))
            .orElseThrow { IllegalArgumentException("User not found with id ${user.id}") }

        val updated = existing.copy(
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            isActive = user.isActive
        )
        return repository.save(updated).toDomain()
    }

    override fun findById(id: Long): User? =
        repository.findById(id).orElse(null)?.toDomain()

    override fun findAll(): List<User> =
        repository.findAll().map { it.toDomain() }

    override fun findByEmail(email: String): User? =
        repository.findByEmail(email)?.toDomain()

    override fun deleteById(id: Long) =
        repository.deleteById(id)

    private fun UserJpaEntity.toDomain() = User(
        id = this.id,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        isActive = this.isActive
    )
}

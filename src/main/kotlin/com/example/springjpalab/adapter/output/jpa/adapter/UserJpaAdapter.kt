package com.example.springjpalab.adapter.output.jpa.adapter


import com.example.springjpalab.adapter.output.jpa.entity.UserJpaEntity
import com.example.springjpalab.adapter.output.jpa.repository.UserJpaRepository
import com.example.springjpalab.domain.model.User
import com.example.springjpalab.domain.port.UserRepositoryPort
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component


@Component
@Profile("test", "db")
class UserJpaAdapter(
    private val repository: UserJpaRepository
) : UserRepositoryPort {

    override fun create(user: User): User {
        val entity = UserJpaEntity(
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            isActive = user.isActive,
            passwordHash = user.hashedPassword
        )
        val saved = repository.save(entity)
        return saved.toDomain()
    }

    override fun update(user: User): User {
        val entity = repository.findById(user.id).orElseThrow {
            NoSuchElementException("User with id=${user.id} not found")
        }

        val updated = entity.copy(
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
        isActive = this.isActive,
        hashedPassword = this.passwordHash,
        role = this.role
    )
}

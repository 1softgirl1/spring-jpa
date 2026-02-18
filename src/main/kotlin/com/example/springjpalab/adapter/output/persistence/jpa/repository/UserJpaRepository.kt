package com.example.springjpalab.adapter.output.persistence.jpa.repository

import com.example.springjpalab.adapter.output.persistence.jpa.entity.UserJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    fun findByEmail(email: String): UserJpaEntity?
}
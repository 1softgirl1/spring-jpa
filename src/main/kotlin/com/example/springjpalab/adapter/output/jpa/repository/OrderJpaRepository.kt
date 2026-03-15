package com.example.springjpalab.adapter.output.jpa.repository

import com.example.springjpalab.adapter.output.jpa.entity.OrderJpaEntity
import com.example.springjpalab.adapter.output.jpa.entity.OrderStatus
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderJpaRepository: JpaRepository<OrderJpaEntity, Long> {


    @EntityGraph(attributePaths = ["user", "dishes"])
    fun findByUserId(userId: Long): List<OrderJpaEntity>

    @EntityGraph(attributePaths = ["user", "dishes"])
    fun findByStatus(status: OrderStatus): List<OrderJpaEntity>

    fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<OrderJpaEntity>

}
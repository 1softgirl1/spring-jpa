package com.example.springjpalab.adapter.output.jpa.repository

import com.example.springjpalab.adapter.output.jpa.entity.ProcessedEventJpaEntity
import com.example.springjpalab.domain.model.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessedEventJpaRepository : JpaRepository<ProcessedEventJpaEntity, Long> {
    fun existsByOrderIdAndNewStatus(orderId: Long, newStatus: OrderStatus): Boolean
}
package com.example.springjpalab.adapter.output.jpa.entity

import com.example.springjpalab.domain.model.OrderStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Id
import java.time.LocalDateTime



@Entity
@Table(
    name = "processed_events",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_processed_events_order_status",
            columnNames = ["order_id", "new_status"]
        )
    ]
)
class ProcessedEventJpaEntity(

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:Column(name = "order_id", nullable = false)
    val orderId: Long,

    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "new_status", nullable = false)
    val newStatus: OrderStatus,

    @field:Column(name = "processed_at", nullable = false)
    val processedAt: LocalDateTime = LocalDateTime.now()
)
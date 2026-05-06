package com.example.springjpalab.adapter.output.jpa.repository

import com.example.springjpalab.adapter.output.jpa.entity.RestaurantJpaEntity
import com.example.springjpalab.domain.model.Restaurant
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RestaurantJpaRepository : JpaRepository<RestaurantJpaEntity, Long>  {
    fun findByName(name: String): RestaurantJpaEntity?

    @Query("SELECT r FROM RestaurantJpaEntity r LEFT JOIN FETCH r.dishes WHERE r.id = :id")
    fun findByIdFetchDishes(@Param("id") id: Long): RestaurantJpaEntity?
}
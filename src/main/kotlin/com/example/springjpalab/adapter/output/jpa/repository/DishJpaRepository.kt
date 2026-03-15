package com.example.springjpalab.adapter.output.jpa.repository

import com.example.springjpalab.adapter.output.jpa.entity.DishJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DishJpaRepository : JpaRepository<DishJpaEntity, Long> {
    // JPQL метод для фильтра по части имени
    @Query("SELECT d FROM DishJpaEntity d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    fun findByNamePart(@Param("namePart") namePart: String): List<DishJpaEntity>

    @Query("SELECT d FROM DishJpaEntity d WHERE LOWER(d.name) = LOWER(:name)")
    fun findByName(@Param("name") name: String): DishJpaEntity?

    @Query("""SELECT d FROM DishJpaEntity d WHERE LOWER(d.name) = LOWER(:name) AND d.restaurant.id = :restaurantId""")
    fun findByNameAndRestaurantId(
        @Param("name") name: String,
        @Param("restaurantId") restaurantId: Long
    ): DishJpaEntity?

}
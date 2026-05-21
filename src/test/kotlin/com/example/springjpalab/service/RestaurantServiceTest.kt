package com.example.springjpalab.service

import com.example.springjpalab.application.service.RestaurantService
import io.mockk.impl.annotations.InjectMockKs
import com.example.springjpalab.domain.exception.AlreadyExistsException
import com.example.springjpalab.domain.exception.NotFoundException
import com.example.springjpalab.domain.model.Restaurant
import com.example.springjpalab.domain.port.RestaurantRepositoryPort
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.impl.annotations.MockK
import java.util.*

@ExtendWith(MockKExtension::class)
class RestaurantServiceTest {

    @MockK
    lateinit var repository: RestaurantRepositoryPort

    @InjectMockKs
    lateinit var service: RestaurantService

    private val sampleRestaurant = Restaurant(id = 1L, name = "Pizza Place", address = "ул. Ленина, 1", dishes = emptyList())

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `findAll возвращает список ресторанов`() {
        val restaurants = listOf(sampleRestaurant)
        every { repository.findAll() } returns restaurants

        val result = service.findAll()

        assertEquals(1, result.size)
        assertEquals("Pizza Place", result[0].name)
        verify { repository.findAll() }
    }

    @Test
    fun `findById возвращает ресторан, если существует`() {
        every { repository.findById(1L) } returns sampleRestaurant

        val result = service.findById(1L)

        assertEquals(1L, result.id)
        assertEquals("Pizza Place", result.name)
        verify { repository.findById(1L) }
    }

    @Test
    fun `findById бросает NotFoundException, если ресторан не найден`() {
        every { repository.findById(999L) } returns null

        assertThrows<NotFoundException> { service.findById(999L) }
        verify { repository.findById(999L) }
    }

    @Test
    fun `findByName возвращает ресторан, если существует`() {
        every { repository.findByName("Pizza Place") } returns sampleRestaurant

        val result = service.findByName("Pizza Place")

        assertEquals("Pizza Place", result.name)
        verify { repository.findByName("Pizza Place") }
    }

    @Test
    fun `findByName бросает NotFoundException, если ресторан не найден`() {
        every { repository.findByName("NonExist") } returns null

        assertThrows<NotFoundException> { service.findByName("NonExist") }
        verify { repository.findByName("NonExist") }
    }

    @Test
    fun `create успешно создает новый ресторан`() {
        val newRestaurant = Restaurant(id = 0, name = "Sushi Bar", address = "ул. Мира, 5", dishes = emptyList())
        val savedRestaurant = newRestaurant.copy(id = 2L)

        every { repository.findByName(newRestaurant.name) } returns null
        every { repository.create(any()) } returns savedRestaurant

        val result = service.create(newRestaurant)

        assertEquals(2L, result.id)
        assertEquals("Sushi Bar", result.name)
        verify { repository.findByName("Sushi Bar") }
        verify { repository.create(match { it.name == "Sushi Bar" }) }
    }

    @Test
    fun `create бросает AlreadyExistsException при дублировании имени`() {
        every { repository.findByName("Pizza Place") } returns sampleRestaurant

        assertThrows<AlreadyExistsException> { service.create(sampleRestaurant) }
        verify { repository.findByName("Pizza Place") }
        verify(exactly = 0) { repository.create(any()) }
    }

    @Test
    fun `update успешно обновляет ресторан`() {
        val updatedRestaurant = sampleRestaurant.copy(name = "Pizza Planet")
        every { repository.update(any()) } returns updatedRestaurant

        val result = service.update(1L, updatedRestaurant)

        assertEquals("Pizza Planet", result.name)
        verify { repository.update(match { it.id == 1L && it.name == "Pizza Planet" }) }
    }

    @Test
    fun `update бросает NotFoundException если ресторан не найден`() {
        val updatedRestaurant = sampleRestaurant.copy(name = "Pizza Planet")
        every { repository.update(any()) } returns null

        assertThrows<NotFoundException> { service.update(999L, updatedRestaurant) }
        verify { repository.update(match { it.id == 999L }) }
    }

    @Test
    fun `delete успешно удаляет ресторан`() {
        every { repository.deleteById(1L) } returns true

        service.delete(1L)

        verify { repository.deleteById(1L) }
    }

    @Test
    fun `delete бросает NotFoundException если ресторан не найден`() {
        every { repository.deleteById(999L) } returns false

        assertThrows<NotFoundException> { service.delete(999L) }
        verify { repository.deleteById(999L) }
    }
}
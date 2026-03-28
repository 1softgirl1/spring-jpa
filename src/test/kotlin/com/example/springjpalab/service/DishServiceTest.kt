package com.example.springjpalab.service
import com.example.springjpalab.adapter.output.jpa.entity.DishJpaEntity
import com.example.springjpalab.adapter.output.jpa.entity.OrderJpaEntity
import com.example.springjpalab.application.service.DishService
import com.example.springjpalab.application.service.OrderService
import io.mockk.impl.annotations.InjectMockKs
import com.example.springjpalab.domain.exception.AlreadyExistsException
import com.example.springjpalab.domain.exception.NotFoundException
import com.example.springjpalab.domain.model.Dish
import com.example.springjpalab.domain.port.DishRepositoryPort
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.impl.annotations.MockK

import kotlinx.coroutines.test.runTest

import kotlin.test.assertFalse


@ExtendWith(MockKExtension::class)
class DishServiceTest {

    @MockK
    lateinit var orderService: OrderService

    @MockK
    lateinit var repository: DishRepositoryPort

    @InjectMockKs
    lateinit var service: DishService

    private val sampleDish = Dish(id = 1L, name = "sampleDish", description = "sample dish", 100.toBigDecimal(), isAvailable = true, restaurantId = 0)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `findByName возвращает блюдо по названию, если существует`() {
        every { repository.findByName("sampleDish") } returns sampleDish

        val result = service.findByName("sampleDish")

        assertEquals(1L, result?.id)
        assertEquals("sampleDish", result?.name)
        assertEquals("sample dish", result?.description)
        assertEquals(true, result?.isAvailable)
        assertEquals(0, result?.restaurantId)

        verify { repository.findByName("sampleDish") }
    }


    @Test
    fun `findByNamePart возвращает блюдо по части названия, если существует`() {
        every { repository.findByNamePart("Dish") } returns listOf(sampleDish)

        val result = service.findByNamePart("Dish")

        assertNotNull(result)

        assertEquals(1, result!!.size)

        val dish = result[0]
        assertEquals(1L, dish?.id)
        assertEquals("sampleDish", dish?.name)
        assertEquals("sample dish", dish?.description)
        assertEquals(true, dish?.isAvailable)
        assertEquals(0, dish?.restaurantId)

        verify { repository.findByNamePart("Dish") }
    }

    @Test
    fun `findAll возвращает список блюд`() {
        val dishes = listOf(sampleDish)
        every { repository.findAll() } returns dishes

        val result = service.findAll()

        assertEquals(1, result.size)
        assertEquals("sampleDish", result[0].name)
        assertEquals("sample dish", result[0].description)
        assertEquals(true, result[0].isAvailable)
        assertEquals(0, result[0].restaurantId)

        verify { repository.findAll() }
    }



    @Test
    fun `findById возвращает блюдо по id, если существует`() {
        every { repository.findById(1L) } returns sampleDish

        val result = service.findById(1L)

        assertEquals(1L, result.id)
        assertEquals("sampleDish", result.name)
        assertEquals("sample dish", result.description)
        assertEquals(true, result.isAvailable)
        assertEquals(0, result.restaurantId)
        verify { repository.findById(1L) }
    }

    @Test
    fun `findById бросает NotFoundException, если блюдо не найдено`() {
        every { repository.findById(999L) } returns null

        assertThrows<NotFoundException> { service.findById(999L) }
        verify { repository.findById(999L) }
    }

    @Test
    fun `create успешно создает новое блюдо`() {
        val newDish = Dish(id = 0, name = "newDish", description = "new sample dish", 200.toBigDecimal(), isAvailable = true, restaurantId = 0)
        val savedDish = newDish.copy(id = 2L)

        every { repository.findByNameAndRestaurantId(newDish.name, newDish.restaurantId) } returns null
        every { repository.create(any()) } returns savedDish

        val result = service.create(newDish)

        assertEquals(2L, result.id)
        assertEquals("newDish", result.name)
        assertEquals("new sample dish", result.description)
        assertEquals(true, result.isAvailable)
        assertEquals(0, result.restaurantId)

        verify { repository.findByNameAndRestaurantId("newDish", 0) }
        verify { repository.create(match { it.name == "newDish" }) }
    }

    @Test
    fun `create бросает AlreadyExistsException при дублировании имени`() {
        every { repository.findByNameAndRestaurantId("sampleDish", 0) } returns sampleDish

        assertThrows<AlreadyExistsException> { service.create(sampleDish) }

        verify { repository.findByNameAndRestaurantId("sampleDish", 0) }
        verify(exactly = 0) { repository.create(any()) }
    }

    @Test
    fun `update успешно обновляет блюдо`() {
        val updatedDish = sampleDish.copy(name = "updatedDish")
        every { repository.findById(1L) } returns sampleDish
        every { repository.update(any()) } returns updatedDish

        val result = service.update(1L, updatedDish)

        assertEquals("updatedDish", result.name)
        verify { repository.findById(1L) }
        verify { repository.update(match { it.id == 1L && it.name == "updatedDish" }) }
    }

    @Test
    fun `update бросает NotFoundException если блюдо не найдено`() {
        val updatedDish = sampleDish.copy(name = "updatedDish")
        every { repository.findById(999L) } returns null

        assertThrows<NotFoundException> { service.update(999L, updatedDish) }
        verify { repository.findById(999L) }
        verify(exactly = 0) { repository.update(any()) }
    }

    @Test
    fun `delete успешно удаляет блюдо и обновляет заказы`() = runTest {
        val dishEntity = mockk<DishJpaEntity>(relaxed = true)
        val orderEntity = mockk<OrderJpaEntity>(relaxed = true)

        val dishesList = mutableListOf(dishEntity)

        every { repository.findEntityById(1L) } returns dishEntity
        every { dishEntity.orders } returns mutableListOf(orderEntity)

        every { orderEntity.dishes } returns dishesList
        every { orderEntity.id } returns 10L
        every { orderEntity.toDomain() } returns mockk()

        coEvery { orderService.update(10L, any()) } returns mockk()

        every { repository.deleteById(any()) } returns true

        service.delete(1L)

        assertFalse(dishesList.contains(dishEntity))

        verify { repository.findEntityById(1L) }
        coVerify { orderService.update(10L, any()) }
        verify { repository.deleteById(1L) }
    }

    @Test
    fun `delete бросает NotFoundException если блюдо не найдено`() {
        every { repository.findEntityById(999L) } returns null

        assertThrows<NotFoundException> {
            service.delete(999L)
        }

        verify { repository.findEntityById(999L) }
        verify(exactly = 0) { repository.deleteById(any()) }
        coVerify(exactly = 0) { orderService.update(any(), any()) }
    }





}
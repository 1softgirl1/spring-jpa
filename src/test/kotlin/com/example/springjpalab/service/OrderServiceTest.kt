package com.example.springjpalab.service
import com.example.springjpalab.domain.model.OrderStatus
import com.example.springjpalab.application.service.NotificationService
import com.example.springjpalab.application.service.OrderService
import com.example.springjpalab.application.service.UserService
import com.example.springjpalab.domain.exception.NotFoundException
import com.example.springjpalab.domain.model.Order
import com.example.springjpalab.domain.model.Role
import com.example.springjpalab.domain.model.User
import com.example.springjpalab.domain.port.DishRepositoryPort
import com.example.springjpalab.domain.port.OrderRepositoryPort
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class OrderServiceTest {
    @MockK
    lateinit var repository: OrderRepositoryPort

    @MockK
    lateinit var dishRepository: DishRepositoryPort

    @MockK(relaxed = true)
    lateinit var notificationService: NotificationService

    @MockK
    lateinit var userService: UserService

    @InjectMockKs
    lateinit var service: OrderService

    private val sampleOrder = Order(id = 1L, status = OrderStatus.PENDING, createdAt = LocalDateTime.now(), userId = 0, dishes = emptyList())
    private val sampleUser = User(
        id = 0L,
        email = "user@example.com",
        firstName = "Test",
        lastName = "User",
        isActive = true,
        hashedPassword = "hash",
        role = Role.USER
    )

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        every { userService.findById(any()) } returns sampleUser
    }

    @Test
    fun `findAll возвращает список заказов`() {
        val orders = listOf(sampleOrder)
        every { repository.findAll() } returns orders

        val result = service.findAll()

        assertEquals(1, result.size)
        assertEquals(OrderStatus.PENDING, result[0].status)
        verify { repository.findAll() }
    }

    @Test
    fun `findById возвращает заказ, если существует`() {
        every { repository.findById(1L) } returns sampleOrder

        val result = service.findById(1L)

        assertEquals(1L, result.id)
        assertEquals(OrderStatus.PENDING, result.status)
        verify { repository.findById(1L) }
    }

    @Test
    fun `findById бросает NotFoundException, если заказ не найден`() {
        every { repository.findById(999L) } returns null

        val exception = assertThrows<NotFoundException> {
            service.findById(999L)
        }

        assertEquals("Заказ с id=999 не найден", exception.message)
        verify { repository.findById(999L) }
    }

    @Test
    fun `findByUserId возвращает заказы по userId`() {
        val orders = listOf(sampleOrder)
        every { repository.findByUserId(0L) } returns orders

        val result = service.findByUserId(0L)

        assertEquals(1, result.size)
        assertEquals(OrderStatus.PENDING, result[0].status)
        verify { repository.findByUserId(0L) }
    }

    @Test
    fun `findByStatus возвращает заказы по статусу`() {
        val orders = listOf(sampleOrder)
        every { repository.findByStatus(OrderStatus.PENDING) } returns orders

        val result = service.findByStatus(OrderStatus.PENDING)

        assertEquals(1, result.size)
        assertEquals(OrderStatus.PENDING, result[0].status)
        verify { repository.findByStatus(OrderStatus.PENDING) }
    }

     @Test
    fun `findByUserIdAndStatus возвращает заказы по userId и статусу`() {
        val orders = listOf(sampleOrder)
         every { repository.findByUserIdAndStatus(0L, OrderStatus.PENDING) } returns orders

         val result = service.findByUserIdAndStatus(0L, OrderStatus.PENDING)

         assertEquals(1, result.size)
         assertEquals(OrderStatus.PENDING, result[0].status)
         verify { repository.findByUserIdAndStatus(0L, OrderStatus.PENDING) }
    }

    @Test
    fun `create успешно создает новый заказ`() {
        val newOrder = Order(id = 0, status = OrderStatus.PENDING, createdAt = LocalDateTime.now(), userId = 1, dishes = emptyList())
        val savedOrder = newOrder.copy(id = 2L)
        every { repository.create(newOrder) } returns savedOrder

        val result = service.create(newOrder)

        assertEquals(2L, result.id)
        assertEquals(OrderStatus.PENDING, result.status)
        assertEquals(1, result.userId)
        verify { repository.create(newOrder) }
    }

     @Test
    fun `update успешно обновляет заказ`() {
        val updatedOrder = sampleOrder.copy(status = OrderStatus.CONFIRMED)
        every { repository.findById(1L) } returns sampleOrder
        every { repository.update(any()) } returns updatedOrder

         val result = service.update(1L, updatedOrder)
         assertEquals(updatedOrder, result)
         assertEquals(OrderStatus.CONFIRMED, result.status)
         assertEquals(1L, result.id)
         verify { repository.findById(1L) }
         verify { repository.update(match { it.id == 1L }) }
         verify { repository.update(match { it.status == OrderStatus.CONFIRMED }) }
    }

    @Test
    fun `update бросает NotFoundException если заказ не найден`() {
        val updatedOrder = sampleOrder.copy(status = OrderStatus.CONFIRMED)
        every { repository.findById(999L) } returns null

        val exception = assertThrows<NotFoundException> {
            service.update(999L, updatedOrder)
        }

        assertEquals("Заказ с id=999 не найден", exception.message)
        verify { repository.findById(999L) }
        verify(exactly = 0) { repository.update(any()) }
    }




}


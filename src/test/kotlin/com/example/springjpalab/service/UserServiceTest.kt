package com.example.springjpalab.service

import com.example.springjpalab.application.service.UserService
import com.example.springjpalab.domain.exception.AlreadyExistsException
import com.example.springjpalab.domain.exception.NotFoundException
import com.example.springjpalab.domain.model.Role
import io.mockk.impl.annotations.InjectMockKs
import com.example.springjpalab.domain.model.User
import com.example.springjpalab.domain.port.UserRepositoryPort
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder


@ExtendWith(MockKExtension::class)
class UserServiceTest {
    
    @MockK
    lateinit var repository: UserRepositoryPort

    @InjectMockKs
    lateinit var service: UserService

    val hash = BCryptPasswordEncoder().encode("password")!!

    private val sampleUser = User(id = 1L, email = "user@example.com", firstName = "Ivan", lastName = "Petrov", hashedPassword = hash , role = Role.USER, isActive = true)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `findAll возвращает список пользователей`() {
        val users = listOf(sampleUser)
        every { repository.findAll() } returns users

        val result = service.findAll()

        assertEquals(1, result.size)
        assertEquals("user@example.com", result[0].email)
        assertEquals("Ivan", result[0].firstName)
        assertEquals("Petrov", result[0].lastName)
        assertEquals(true, result[0].isActive)

        verify { repository.findAll() }
    }


    @Test
    fun `findById возвращает пользователя, если существует`() {
        every { repository.findById(1L) } returns sampleUser

        val result = service.findById(1L)

        assertEquals(1L, result.id)
        assertEquals("user@example.com", result.email)
        assertEquals("Ivan", result.firstName)
        assertEquals("Petrov", result.lastName)
        assertEquals(true, result.isActive)

        verify { repository.findById(1L) }
    }

    @Test
    fun `findById бросает NotFoundException, если user не найден`() {
        every { repository.findById(999L) } returns null

        assertThrows<NotFoundException> { service.findById(999L) }
        verify { repository.findById(999L) }
    }

    @Test
    fun `findByEmail возвращает user, если существует`() {
        every { repository.findByEmail("user@example.com") } returns sampleUser

        val result = service.findByEmail("user@example.com")

        assertEquals(1L, result?.id)
        assertEquals("Ivan", result?.firstName)
        assertEquals("Petrov", result?.lastName)
        assertEquals(true, result?.isActive)

        verify { repository.findByEmail("user@example.com") }
    }


    @Test
    fun `findByEmail возвращает null если пользователь не найден`() {
        every { repository.findByEmail("nonexistent@ya.ru") } returns null

        val result = service.findByEmail("nonexistent@ya.ru")
        assertNull(result)
    }


    @Test
    fun `create успешно создает нового пользователя`() {
        val hash = BCryptPasswordEncoder().encode("passwwword")!!
        val newUser = User(id = 0, email = "pog0sian@yandex.ru", firstName = "David", lastName = "Pogosian", hashedPassword = hash, role = Role.USER,isActive = true)
        val savedUser = newUser.copy(id = 2L)

        every { repository.findByEmail(newUser.email) } returns null
        every { repository.create(any()) } returns savedUser

        val result = service.create(newUser)

        assertEquals(2L, result.id)
        assertEquals("pog0sian@yandex.ru", result.email)
        assertEquals("David", result.firstName)
        assertEquals("Pogosian", result.lastName)
        assertEquals(true, result.isActive)

        verify { repository.findByEmail("pog0sian@yandex.ru") }
        verify { repository.create(match { it.email == "pog0sian@yandex.ru" }) }
    }

    @Test
    fun `create бросает AlreadyExistsException при дублировании имени`() {

        every { repository.findByEmail("user@example.com") } returns sampleUser

        assertThrows<AlreadyExistsException> { service.create(sampleUser) }

        verify { repository.findByEmail("user@example.com") }
        verify(exactly = 0) { repository.create(any()) }
    }

    @Test
    fun `update успешно обновляет пользователя`() {
        val updatedUser = sampleUser.copy(email = "pog@ya.ru")
        every { repository.findById(1L) } returns sampleUser

        every { repository.update(any()) } returns updatedUser.copy(id = 1L)

        val result = service.update(1L, updatedUser)

        assertEquals("pog@ya.ru", result.email)
        verify { repository.findById(1L) }
        verify { repository.update(match { it.id == 1L && it.email == "pog@ya.ru" }) }
    }

    @Test
    fun `update бросает NotFoundException если пользователь не найден`() {
        val updatedUser = sampleUser.copy(email = "pog@ya.ru")
        every { repository.findById(999L) } returns null

        assertThrows<NotFoundException> { service.update(999L, updatedUser) }

        verify(exactly = 1) { repository.findById(999L) }
        verify(exactly = 0) { repository.update(any()) }
    }

    @Test
    fun `delete успешно удаляет пользователя`() {
        every { repository.findById(1L) } returns sampleUser
        every { repository.deleteById(1L) } just Runs

        service.delete(1L)

        verify { repository.findById(1L) }
        verify { repository.deleteById(1L) }
    }

    @Test
    fun `delete бросает NotFoundException если пользователь не найден`() {
        every { repository.findById(999L) } returns null

        assertThrows<NotFoundException> { service.delete(999L) }

        verify { repository.findById(999L) }
        verify(exactly = 0) { repository.deleteById(any()) }
    }



}
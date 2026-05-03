package com.example.springjpalab.cache

import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.cache.CacheManager
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class UserCacheTest {

    companion object {
        @Container
        @JvmStatic
        val redis = GenericContainer("redis:7-alpine")
            .withExposedPorts(6379)

        @DynamicPropertySource
        @JvmStatic
        fun redisProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redis.host }
            registry.add("spring.data.redis.port") { redis.firstMappedPort }
        }
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var cacheManager: CacheManager

    @BeforeEach
    fun clearRedisCache() {
        cacheManager.getCache("users")?.clear()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `повторный запрос списка пользователей не идёт в БД`() {
        mockMvc.perform(get("/api/v1/users").with(user("admin").roles("ADMIN"))).andExpect(status().isOk)
        mockMvc.perform(get("/api/v1/users").with(user("admin").roles("ADMIN"))).andExpect(status().isOk)

        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            assertNotNull(cacheManager.getCache("users")?.get(SimpleKey.EMPTY))
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `создание пользователя инвалидирует кэш`() {
        val registerResponse = mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"cache-user-${System.nanoTime()}@mail.test","password":"qwerty123","name":"Cache User"}""")
        ).andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        val email = "\"email\":\"([^\"]+)\"".toRegex()
            .find(registerResponse)
            ?.groupValues
            ?.get(1)
            ?: error("Не удалось извлечь email зарегистрированного пользователя")

        val usersResponse = mockMvc.perform(
            get("/api/v1/users").with(user("admin").roles("ADMIN"))
        ).andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        val idRegex = "\\{\"id\":(\\d+),\"email\":\"${Regex.escape(email)}\"".toRegex()
        val userId = idRegex.find(usersResponse)
            ?.groupValues
            ?.get(1)
            ?: error("Не удалось определить id пользователя по email")

        mockMvc.perform(get("/api/v1/users/$userId").with(user("admin").roles("ADMIN"))).andExpect(status().isOk)
        mockMvc.perform(get("/api/v1/users/$userId").with(user("admin").roles("ADMIN"))).andExpect(status().isOk)
        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            assertNotNull(cacheManager.getCache("users")?.get(userId.toLong()))
        }

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"cache-user-${System.nanoTime()}@mail.test","password":"qwerty123","name":"Cache User 2"}""")
        ).andExpect(status().isCreated)

        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            assertNull(cacheManager.getCache("users")?.get(userId.toLong()))
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `после update пользователя кэш содержит запись по id`() {
        val registerResponse = mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"cache-user-${System.nanoTime()}@mail.test","password":"qwerty123","name":"Cache User"}""")
        ).andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        val email = "\"email\":\"([^\"]+)\"".toRegex()
            .find(registerResponse)
            ?.groupValues
            ?.get(1)
            ?: error("Не удалось извлечь email зарегистрированного пользователя")

        val usersResponse = mockMvc.perform(
            get("/api/v1/users").with(user("admin").roles("ADMIN"))
        ).andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        val idRegex = "\\{\"id\":(\\d+),\"email\":\"${Regex.escape(email)}\"".toRegex()
        val userId = idRegex.find(usersResponse)
            ?.groupValues
            ?.get(1)
            ?: error("Не удалось определить id пользователя по email")

        mockMvc.perform(
            put("/api/v1/users/$userId")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","firstName":"Updated","lastName":"User","isActive":true,"role":"USER"}""")
        ).andExpect(status().isOk)

        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            assertNotNull(cacheManager.getCache("users")?.get(userId.toLong()))
        }
    }
}

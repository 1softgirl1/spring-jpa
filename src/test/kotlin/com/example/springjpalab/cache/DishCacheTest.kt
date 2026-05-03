package com.example.springjpalab.cache

import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.cache.CacheManager
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
class DishCacheTest {

    companion object {
        private const val LIST_KEY = "all"

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
        cacheManager.getCache("dishes")?.clear()
        cacheManager.getCache("restaurants")?.clear()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `повторный запрос списка блюд не идёт в БД`() {
        mockMvc.perform(get("/api/v1/dishes")).andExpect(status().isOk)
        mockMvc.perform(get("/api/v1/dishes")).andExpect(status().isOk)

        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            val cacheEntry = cacheManager.getCache("dishes")?.get(LIST_KEY)
            assertNotNull(cacheEntry)
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `создание блюда инвалидирует кэш`() {
        mockMvc.perform(get("/api/v1/dishes")).andExpect(status().isOk)
        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            assertNotNull(cacheManager.getCache("dishes")?.get(LIST_KEY))
        }

        val restaurantResponse = mockMvc.perform(
            post("/api/v1/restaurants")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Рест-${System.nanoTime()}", "address": "ул. Теста, 10"}""")
        ).andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        val restaurantId = "\\\"id\\\":(\\d+)".toRegex()
            .find(restaurantResponse)
            ?.groupValues
            ?.get(1)
            ?: error("Не удалось извлечь id ресторана")

        mockMvc.perform(
            post("/api/v1/restaurants/$restaurantId/dishes")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Блюдо-${System.nanoTime()}","description":"Описание","price":100.0,"isAvailable":true}""")
        ).andExpect(status().isCreated)

        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            assertNull(cacheManager.getCache("dishes")?.get(LIST_KEY))
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `после update блюда кэш очищается`() {
        val restaurantResponse = mockMvc.perform(
            post("/api/v1/restaurants")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Рест-${System.nanoTime()}", "address": "ул. Теста, 11"}""")
        ).andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        val restaurantId = "\\\"id\\\":(\\d+)".toRegex()
            .find(restaurantResponse)
            ?.groupValues
            ?.get(1)
            ?: error("Не удалось извлечь id ресторана")

        val dishResponse = mockMvc.perform(
            post("/api/v1/restaurants/$restaurantId/dishes")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Блюдо-${System.nanoTime()}","description":"Описание","price":120.0,"isAvailable":true}""")
        ).andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        val dishId = "\\\"id\\\":(\\d+)".toRegex()
            .find(dishResponse)
            ?.groupValues
            ?.get(1)
            ?: error("Не удалось извлечь id блюда")

        mockMvc.perform(get("/api/v1/dishes")).andExpect(status().isOk)
        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            assertNotNull(cacheManager.getCache("dishes")?.get(LIST_KEY))
        }

        mockMvc.perform(
            put("/api/v1/dishes/$dishId")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Обновлённое блюдо","description":"Новое описание","price":150.0,"isAvailable":true}""")
        ).andExpect(status().isOk)

        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            assertNull(cacheManager.getCache("dishes")?.get(LIST_KEY))
        }
    }
}

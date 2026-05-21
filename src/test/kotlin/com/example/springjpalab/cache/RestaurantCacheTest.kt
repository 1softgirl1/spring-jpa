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
class RestaurantCacheTest {

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
        cacheManager.getCache("restaurants")?.clear()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `повторный запрос списка ресторанов не идёт в БД`() {
        mockMvc.perform(get("/api/v1/restaurants")).andExpect(status().isOk)
        mockMvc.perform(get("/api/v1/restaurants")).andExpect(status().isOk)

        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            assertNotNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `создание ресторана инвалидирует кэш`() {
        val createResponse = mockMvc.perform(
            post("/api/v1/restaurants")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Для инвалидации-${System.nanoTime()}", "address": "ул. Теста, 1"}""")
        ).andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        val restaurantId = "\"id\":(\\d+)".toRegex()
            .find(createResponse)
            ?.groupValues
            ?.get(1)
            ?: error("Не удалось извлечь id из ответа create")

        mockMvc.perform(get("/api/v1/restaurants/$restaurantId")).andExpect(status().isOk)
        mockMvc.perform(get("/api/v1/restaurants/$restaurantId")).andExpect(status().isOk)
        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            assertNotNull(cacheManager.getCache("restaurants")?.get(restaurantId.toLong()))
        }

        mockMvc.perform(
            post("/api/v1/restaurants")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Новый-${System.nanoTime()}", "address": "ул. Теста, 2"}""")
        ).andExpect(status().isCreated)

        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            assertNull(cacheManager.getCache("restaurants")?.get(restaurantId.toLong()))
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `после update кэш содержит обновлённые данные`() {
        val createResponse = mockMvc.perform(
            post("/api/v1/restaurants")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Для обновления", "address": "ул. Первая, 1"}""")
        ).andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        val restaurantId = "\"id\":(\\d+)".toRegex()
            .find(createResponse)
            ?.groupValues
            ?.get(1)
            ?: error("Не удалось извлечь id из ответа create")

        mockMvc.perform(
            put("/api/v1/restaurants/$restaurantId")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Обновлённый", "address": "ул. Вторая, 2"}""")
        ).andExpect(status().isOk)

        await().atMost(Duration.ofSeconds(2)).untilAsserted {
            assertNotNull(cacheManager.getCache("restaurants")?.get(restaurantId.toLong()))
        }
    }
}

package com.example.springjpalab.controller

import com.jayway.jsonpath.JsonPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.*
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.testSecurityContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.GenericContainer


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class RestaurantIntegrationTest {

    companion object {
        @Container // Управляет стартом/стопом контейнера
        @ServiceConnection // МАГИЯ: Автоматически прокидывает настройки в Spring DataSource
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:17").apply {
            // Опционально: можно задать имя БД или пароль,
            // но благодаря @ServiceConnection Спрингу всё равно, какие они
            withDatabaseName("integration-tests-db")
            withUsername("test")
            withPassword("test")
        }

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

    private fun withMockUser(request: MockHttpServletRequestBuilder): MockHttpServletRequestBuilder =
        request.with(testSecurityContext())


    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `создание ресторана от ADMIN возвращает 201`() {
        mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Тест", "address": "Улица 1"}""")
        ).andExpect(status().isCreated)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `создание ресторана от USER возвращает 403`() {
        mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Тест", "address": "Улица 1"}""")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `создание ресторана без токена возвращает 401`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Тест", "address": "Улица 1"}""")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET несуществующий ресторан возвращает 404`() {
        mockMvc.perform(get("/api/v1/restaurants/999999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST restaurant с пустым именем возвращает 400 и errors`() {
        mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "", "address": "ул. Тестовая, 1"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.name").exists())
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST restaurant с пустым адресом возвращает 400 и errors`() {
        mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Твиани", "address": ""}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.address").exists())
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST restaurant с невалидным body возвращает 400`() {
        mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{}""")
        )
            .andExpect(status().isBadRequest)
    }


    @Test
    fun `GET все рестораны возвращает 200 и массив`() {
        mockMvc.perform(get("/api/v1/restaurants"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `GET restaurant по id возвращает 200`(){
        val result = mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Find Me", "address": "ул. 2"}""")
        )
            .andExpect(status().isCreated)
            .andReturn()

        val id = JsonPath.read<Int>(result.response.contentAsString, "$.id")

        mockMvc.perform(get("/api/v1/restaurants/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Find Me"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `PUT restaurant обновляет и возвращает 200`() {
        val uniqueName = "Old Name ${System.currentTimeMillis()}"

        val result = mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "$uniqueName", "address": "ул. 3"}""")
        )
            .andExpect(status().isCreated)
            .andReturn()

        val id = JsonPath.read<Int>(result.response.contentAsString, "$.id")

        mockMvc.perform(
            withMockUser(put("/api/v1/restaurants/$id"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "New Name", "address": "ул. 3"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("New Name"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `PUT с пустым body возвращвет 400`(){
        val result = mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Old Name", "address": "ул. 3"}""")
        )
            .andExpect(status().isCreated)
            .andReturn()

        val id = JsonPath.read<Int>(result.response.contentAsString, "$.id")

        mockMvc.perform(
            withMockUser(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/restaurants/$id"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "", "address": ""}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.name").exists())

    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST второго ресторана возвращает 201`() {
        mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "First", "address": "ул. 5"}""")
        ).andExpect(status().isCreated)

        mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Second", "address": "ул. 6"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Second"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST дубликата ресторана возвращает 409`() {
        mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "First1", "address": "ул. 5"}""")
        ).andExpect(status().isCreated)

        mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "First1", "address": "ул. 6"}""")
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `DELETE ресторанов и проверка что они удалены`() {
        val r1 = mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "ToDelete1", "address": "ул. 10"}""")
        ).andExpect(status().isCreated).andReturn()

        val r2 = mockMvc.perform(
            withMockUser(post("/api/v1/restaurants"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "ToDelete2", "address": "ул. 11"}""")
        ).andExpect(status().isCreated).andReturn()

        val id1 = JsonPath.read<Int>(r1.response.contentAsString, "$.id")
        val id2 = JsonPath.read<Int>(r2.response.contentAsString, "$.id")

        mockMvc.perform(withMockUser(delete("/api/v1/restaurants/$id1")))
            .andExpect(status().isNoContent)

        mockMvc.perform(withMockUser(delete("/api/v1/restaurants/$id2")))
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/api/v1/restaurants/$id1"))
            .andExpect(status().isNotFound)

        mockMvc.perform(get("/api/v1/restaurants/$id2"))
            .andExpect(status().isNotFound)
    }



}

package com.example.springjpalab.controller

import com.jayway.jsonpath.JsonPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.*
import kotlin.test.Test
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


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
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `POST restaurant возвращает 201 и создаёт запись`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "New Place", "address": "ул. Тестовая, 1"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("New Place"))
            .andExpect(jsonPath("$.address").value("ул. Тестовая, 1"))
    }

    @Test
    fun `GET несуществующий ресторан возвращает 404`() {
        mockMvc.perform(get("/api/v1/restaurants/999999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    fun `POST restaurant с пустым именем возвращает 400 и errors`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "", "address": "ул. Тестовая, 1"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.name").exists())
    }

    @Test
    fun `POST restaurant с пустым адресом возвращает 400 и errors`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Твиани", "address": ""}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.address").exists())
    }

    @Test
    fun `POST restaurant с невалидным body возвращает 400`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
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
    fun `GET restaurant по id возвращает 200`(){
        val result = mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Find Me", "address": "ул. 2"}""")
        )
            .andReturn()

        val id = JsonPath.read<Int>(result.response.contentAsString, "$.id")

        mockMvc.perform(get("/api/v1/restaurants/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Find Me"))
    }

    @Test
    fun `PUT restaurant обновляет и возвращает 200`() {
        val uniqueName = "Old Name ${System.currentTimeMillis()}"

        val result = mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "$uniqueName", "address": "ул. 3"}""")
        )
            .andExpect(status().isCreated)
            .andReturn()

        val id = JsonPath.read<Int>(result.response.contentAsString, "$.id")

        mockMvc.perform(
            put("/api/v1/restaurants/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "New Name", "address": "ул. 3"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("New Name"))
    }

    @Test
    fun `PUT с пустым body возвращвет 400`(){
        val result = mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Old Name", "address": "ул. 3"}""")
        )
            .andReturn()

        val id = JsonPath.read<Int>(result.response.contentAsString, "$.id")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/restaurants/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "", "address": ""}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.name").exists())

    }

    @Test
    fun `POST второго ресторана возвращает 201`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "First", "address": "ул. 5"}""")
        )

        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Second", "address": "ул. 6"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Second"))
    }

    @Test
    fun `POST дубликата ресторана возвращает 409`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "First", "address": "ул. 5"}""")
        )

        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "First", "address": "ул. 6"}""")
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
    }

    @Test
    fun `DELETE ресторанов и проверка что они удалены`() {
        val r1 = mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "ToDelete1", "address": "ул. 10"}""")
        ).andReturn()

        val r2 = mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "ToDelete2", "address": "ул. 11"}""")
        ).andReturn()

        val id1 = JsonPath.read<Int>(r1.response.contentAsString, "$.id")
        val id2 = JsonPath.read<Int>(r2.response.contentAsString, "$.id")

        mockMvc.perform(delete("/api/v1/restaurants/$id1"))
            .andExpect(status().isNoContent)

        mockMvc.perform(delete("/api/v1/restaurants/$id2"))
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/api/v1/restaurants/$id1"))
            .andExpect(status().isNotFound)

        mockMvc.perform(get("/api/v1/restaurants/$id2"))
            .andExpect(status().isNotFound)
    }



}
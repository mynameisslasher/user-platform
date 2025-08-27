package org.example.userdbapi.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.userdbapi.dto.UserCreateDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.Serializable;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class UserApiIT {

    @Container
    static PostgreSQLContainer<?> pg =  new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("userdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pg::getJdbcUrl);
        registry.add("spring.datasource.username", pg::getUsername);
        registry.add("spring.datasource.password", pg::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.open-in-view", () -> false);
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @Test
    void fullCycle_create_get_delete() throws Exception {
        // reate
        var in = new UserCreateDto("Vanya", "v@mail.ru", 20);
        var resp = mvc.perform(post("/api/users")
                    .contentType(APPLICATION_JSON)
                    .content(om.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        record Out(Long id, String name, String email, Integer age) {}
        var out = om.readValue(resp,  Out.class);

        // get
        mvc.perform(get("/api/users/{id}", out.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("v@mail.ru"));

        // delete
        mvc.perform(delete("/api/users/{id}", out.id()))
                .andExpect(status().isNoContent());

        // get -> 404
        mvc.perform(get("/api/users/{id}", out.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateEmail_conflict409() throws Exception {
        var a = new UserCreateDto("Alice", "example@mail.ru", 20);
        var b = new UserCreateDto("Bob", "example@mail.ru", 21);

        mvc.perform(post("/api/users")
                    .contentType(APPLICATION_JSON)
                    .content(om.writeValueAsString(a)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/users")
                    .contentType(APPLICATION_JSON)
                    .content(om.writeValueAsString(b)))
                .andExpect(status().isConflict());
    }
}

package org.example.userdbapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.userdbapi.dto.UserCreateDto;
import org.example.userdbapi.dto.UserDto;
import org.example.userdbapi.exception.ConflictException;
import org.example.userdbapi.exception.GlobalExceptionHandler;
import org.example.userdbapi.exception.NotFoundException;
import org.example.userdbapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean
    UserService service;

    @Test
    void getById_ok() throws Exception {
        var dto = new UserDto(1L, "Vanya", "v@m.ru", 20, LocalDateTime.now());
        when(service.getUserByID(1L)).thenReturn(dto);

        mvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("v@m.ru"));
    }

    @Test
    void getById_notFound() throws Exception {
        when(service.getUserByID(9L)).thenThrow(new NotFoundException("User 9 not found"));

        mvc.perform(get("/api/users/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User 9 not found"))
                .andExpect(jsonPath("$.status").value("404"));
    }

    @Test
    void getAll_ok() throws Exception {
        var list = List.of(
                new UserDto(1L, "Alice", "a@a.ru", 20, LocalDateTime.now()),
                new UserDto(2L, "Bob", "b@b.ru", 30, LocalDateTime.now())
        );

        when(service.getAllUsers()).thenReturn(list);

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void create_ok() throws Exception {
        var in = new UserCreateDto("Vanya", "v@mail.ru", 20);
        var out = new UserDto(100L, "Vanya", "v@mail.ru", 20, LocalDateTime.now());
        when(service.createUser(any())).thenReturn(out);

        mvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/100"));
    }

    @Test
    void create_conflict() throws Exception {
        var in = new UserCreateDto("Vanya", "vanya@mail.ru", 20);
        when(service.createUser(any())).thenThrow(new ConflictException("Email already exists: vanya@mail.ru"));

        mvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(in)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists: vanya@mail.ru"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void create_badRequest_validation() throws Exception {
        var badJson = """
                    {"name": "Vanya", "email": "", "age": 151}
                """;

        mvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }
}

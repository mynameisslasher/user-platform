package org.example.userdbapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.userdbapi.dto.UserCreateDto;
import org.example.userdbapi.dto.UserDto;
import org.example.userdbapi.dto.UserUpdateDto;
import org.example.userdbapi.exception.ConflictException;
import org.example.userdbapi.exception.NotFoundException;
import org.example.userdbapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    UserService service;

    @Test
    void getAll_returnsList() throws Exception {
        var u = new UserDto(1L, "Bob", "b@gmail.com", 30, LocalDateTime.now());
        when(service.getAllUsers()).thenReturn(List.of(u));

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("b@gmail.com"));
    }

    @Test
    void getById_notFound() throws Exception {
        when(service.getUserByID(99L)).thenThrow(new NotFoundException("User 99 not found"));
        mvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"));
    }

    @Test
    void create_valid_returns201() throws Exception {
        var payload = new UserCreateDto("Alie", "a@gmail.com", 25);
        var created = new UserDto(10L,"Alice", "a@gmail.com", 25, LocalDateTime.now());
        when(service.createUser(any())).thenReturn(created);

        mvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/users/10")))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void create_conflict409_whenDuplicateEmail() throws Exception {
        var payload = new UserCreateDto("Alice", "a@gmail.com", 25);
        when(service.createUser(any())).thenThrow(new ConflictException("Email already exists"));

        mvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void create_badRequest_whenInvalidEmail() throws Exception {
        var payload = new UserCreateDto("Alice","bad_email", 25);

        mvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email", notNullValue()));
    }

    @Test
    void update_ok() throws Exception {
        var upd = new UserUpdateDto("Robert", "r@gmail.com", 23);
        var dto = new UserDto(1L, "Robert", "r@gmail.com", 23, LocalDateTime.now());
        when(service.updateUser(eq(1L), any())).thenReturn(dto);

        mvc.perform(put("/api/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Robert"));
    }

    @Test
    void delete_noContent() throws Exception {
        mvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}

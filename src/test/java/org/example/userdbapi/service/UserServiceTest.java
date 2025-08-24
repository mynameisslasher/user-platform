package org.example.userdbapi.service;

import org.example.userdbapi.dto.UserCreateDto;
import org.example.userdbapi.dto.UserDto;
import org.example.userdbapi.dto.UserUpdateDto;
import org.example.userdbapi.exception.ConflictException;
import org.example.userdbapi.exception.NotFoundException;
import org.example.userdbapi.model.User;
import org.example.userdbapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void getUserById_found() {
        User u = new User();
        u.setId(1L);
        u.setName("Robert");
        u.setEmail("robert@example.com");
        u.setAge(30);
        u.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        UserDto dto = userService.getUserByID(1L);

        assertEquals(1L, dto.id());
        assertEquals("Robert", dto.name());
        assertEquals("robert@example.com", dto.email());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserByID(99L));
    }

    @Test
    void getAllUsers_ok() {
        User a = new User(); a.setId(1L); a.setName("A"); a.setEmail("a@ex.com"); a.setAge(20); a.setCreatedAt(LocalDateTime.now());
        User b = new User(); b.setId(2L); b.setName("B"); b.setEmail("b@ex.com"); b.setAge(21); b.setCreatedAt(LocalDateTime.now());

        when(userRepository.findAll()).thenReturn(List.of(a, b));

        var list = userService.getAllUsers();

        assertEquals(2, list.size());
        assertEquals("A", list.get(0).name());
        assertEquals("B", list.get(1).name());
        verify(userRepository).findAll();
    }

    @Test
    void createUser_ok() {
        UserCreateDto in = new UserCreateDto("Alice", "a@ex.com", 25);

        when(userRepository.existsByEmail("a@ex.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User s = inv.getArgument(0, User.class);
            s.setId(10L);
            s.setCreatedAt(LocalDateTime.now());
            return s;
        });

        UserDto dto = userService.createUser(in);

        assertEquals(10L, dto.id());
        assertEquals("Alice", dto.name());
        assertEquals("a@ex.com", dto.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_conflict_emailExists() {
        UserCreateDto in = new UserCreateDto("Alice", "a@ex.com", 25);
        when(userRepository.existsByEmail("a@ex.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(in));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ok() {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old");
        existing.setEmail("old@ex.com");
        existing.setAge(20);
        existing.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@ex.com")).thenReturn(false);
        when(userRepository.save(existing)).thenReturn(existing);

        UserUpdateDto upd = new UserUpdateDto("New", "new@ex.com", 22);

        UserDto dto = userService.updateUser(1L, upd);

        assertEquals("New", dto.name());
        assertEquals("new@ex.com", dto.email());
        assertEquals(22, dto.age());
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_conflict_emailTaken() {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old");
        existing.setEmail("old@ex.com");
        existing.setAge(20);
        existing.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@ex.com")).thenReturn(true);

        UserUpdateDto upd = new UserUpdateDto("New", "new@ex.com", 22);

        assertThrows(ConflictException.class, () -> userService.updateUser(1L, upd));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_ok() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.existsById(42L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.deleteUser(42L));
        verify(userRepository, never()).deleteById(anyLong());
    }
}

package org.example.userdbapi.service;

import org.example.userdbapi.dto.UserCreateDto;
import org.example.userdbapi.dto.UserDto;
import org.example.userdbapi.dto.UserUpdateDto;
import org.example.userdbapi.exception.ConflictException;
import org.example.userdbapi.exception.NotFoundException;
import org.example.userdbapi.model.User;
import org.example.userdbapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


public class UserServiceTest {

    private UserRepository repo;
    private UserService service;

    @BeforeEach
    void init(){
        repo = mock(UserRepository.class);
        service = new UserService(repo);
    }

    @Test
    void getUserById_Ok(){
        var user = new User();
        user.setId(1L);
        user.setName("Vanya");
        user.setEmail("v@mail.ru");
        user.setAge(20);
        user.setCreatedAt(LocalDateTime.now());

        when(repo.findById(1L)).thenReturn(Optional.of(user));

        UserDto dto = service.getUserByID(1L);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.email()).isEqualTo("v@mail.ru");
        verify(repo).findById(1L);
    }

    @Test
    void getUserById_NotFound(){
        when(repo.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getUserByID(9L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User 9 not found");
    }

    @Test
    void createUser_conflictEmail(){
        var dto = new UserCreateDto("Vanya", "v@mail.ru", 20);
        when(repo.existsByEmail("v@mail.ru")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");

        verify(repo, never()).save(any());
    }

    @Test
    void createUser_ok_savesEntity(){
        var dto = new UserCreateDto("Vanya", "v@mail.ru", 20);
        when(repo.existsByEmail(dto.email())).thenReturn(false);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(repo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(100L);
            u.setCreatedAt(LocalDateTime.now());
            return u;
        });

        UserDto result = service.createUser(dto);

        verify(repo).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Vanya");
        assertThat(result.id()).isEqualTo(100L);
    }

    @Test
    void updateUser_notFound(){
        when(repo.findById(5L)).thenReturn(Optional.empty());
        var dto = new UserUpdateDto("Oleg", "o@mail.ru", 99);
        assertThatThrownBy(() -> service.updateUser(5L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateUser_conflictEmail(){
        var existing = new User();
        existing.setId(1L);
        existing.setName("Ya");
        existing.setEmail("ya@mail.ru");
        existing.setAge(21);

        when(repo.findById(1L)).thenReturn(Optional.of(existing));

        var dto = new UserUpdateDto("Ya2", "ya2@mail.ru", 23);
        when(repo.existsByEmail("ya2@mail.ru")).thenReturn(true);

        assertThatThrownBy(() -> service.updateUser(1L, dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteUser_notFound(){
        when(repo.existsById(7L)).thenReturn(false);
        assertThatThrownBy(() -> service.deleteUser(7L))
                .isInstanceOf(NotFoundException.class);
        verify(repo, never()).deleteById(anyLong());
    }

    @Test
    void deleteUser_ok(){
        when(repo.existsById(7L)).thenReturn(true);
        service.deleteUser(7L);
        verify(repo).deleteById(7L);
    }
}

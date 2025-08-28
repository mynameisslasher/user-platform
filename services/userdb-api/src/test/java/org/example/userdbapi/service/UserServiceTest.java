package org.example.userdbapi.service;

import org.example.userdbapi.dto.UserCreateDto;
import org.example.userdbapi.dto.UserUpdateDto;
import org.example.userdbapi.events.UserEvent;
import org.example.userdbapi.events.UserEventProducer;
import org.example.userdbapi.exception.ConflictException;
import org.example.userdbapi.exception.NotFoundException;
import org.example.userdbapi.mapper.UserMapper;
import org.example.userdbapi.model.User;
import org.example.userdbapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventProducer userEventProducer;

    private UserService service;
    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
        service = new UserService(userRepository, mapper, userEventProducer);
    }

    @Test
    void getUserById_Ok() {
        User u = new User();
        u.setId(1L); u.setName("Bob"); u.setEmail("b@b.com"); u.setAge(20);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        var dto = service.getUserByID(1L);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.email()).isEqualTo("b@b.com");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getUserByID(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllUsers_ok() {
        User u = new User();
        u.setId(1L); u.setName("A"); u.setEmail("a@a.com"); u.setAge(18);
        when(userRepository.findAll()).thenReturn(List.of(u));

        var list = service.getAllUsers();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).email()).isEqualTo("a@a.com");
        verify(userRepository).findAll();
    }

    @Test
    void createUser_conflictEmail() {
        var dto = new UserCreateDto("A", "a@a.com", 20);
        when(userRepository.existsByEmail("a@a.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(dto))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
        verify(userEventProducer, never()).send(any());
    }

    @Test
    void createUser_ok_savesEntity_andSendsEvent() {
        var dto = new UserCreateDto("A", "a@a.com", 20);

        when(userRepository.existsByEmail("a@a.com")).thenReturn(false);

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User arg = inv.getArgument(0);
            arg.setId(42L);
            return arg;
        });

        var result = service.createUser(dto);

        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.email()).isEqualTo("a@a.com");

        ArgumentCaptor<UserEvent> captor = ArgumentCaptor.forClass(UserEvent.class);
        verify(userEventProducer, times(1)).send(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(UserEvent.Type.USER_CREATED);
        assertThat(captor.getValue().getUserId()).isEqualTo(42L);
        assertThat(captor.getValue().getEmail()).isEqualTo("a@a.com");
    }

    @Test
    void updateUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateUser(1L, new UserUpdateDto("X","x@x.com",30)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateUser_conflictEmail() {
        User u = new User();
        u.setId(1L); u.setName("Old"); u.setEmail("old@a.com"); u.setAge(19);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userRepository.existsByEmail("new@a.com")).thenReturn(true);

        assertThatThrownBy(() -> service.updateUser(1L, new UserUpdateDto("New", "new@a.com", 21)))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteUser(99L))
                .isInstanceOf(NotFoundException.class);
        verify(userRepository, never()).deleteById(anyLong());
        verify(userEventProducer, never()).send(any());
    }

    @Test
    void deleteUser_ok_deletes_andSendsEvent() {
        User u = new User();
        u.setId(7L); u.setEmail("x@y.com");
        when(userRepository.findById(7L)).thenReturn(Optional.of(u));

        service.deleteUser(7L);

        verify(userRepository).deleteById(7L);

        ArgumentCaptor<UserEvent> captor = ArgumentCaptor.forClass(UserEvent.class);
        verify(userEventProducer).send(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(UserEvent.Type.USER_DELETED);
        assertThat(captor.getValue().getUserId()).isEqualTo(7L);
        assertThat(captor.getValue().getEmail()).isEqualTo("x@y.com");
    }
}
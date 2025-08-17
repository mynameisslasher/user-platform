package org.example.userdbapi.service;

import org.example.userdbapi.model.User;
import org.example.userdbapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testGetUserById_UserExists() {
        User user = new User();
        user.setId(1L);
        user.setName("Robert");
        user.setEmail("robert@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByID(1L);

        assertTrue(result.isPresent());
        assertEquals("Robert", result.get().getName());
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserById_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByID(1L);

        assertFalse(result.isPresent());
        verify(userRepository).findById(1L);
    }
}

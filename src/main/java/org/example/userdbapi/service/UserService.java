package org.example.userdbapi.service;

import lombok.extern.slf4j.Slf4j;
import org.example.userdbapi.dto.UserCreateDto;
import org.example.userdbapi.dto.UserDto;
import org.example.userdbapi.dto.UserUpdateDto;
import org.example.userdbapi.exception.ConflictException;
import org.example.userdbapi.exception.NotFoundException;
import org.example.userdbapi.mapper.UserMapper;
import org.example.userdbapi.model.User;
import org.example.userdbapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.example.userdbapi.mapper.UserMapper.*;

@Slf4j
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // Dependency Injection через конструктор
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        var list = userRepository.findAll().stream().map(UserMapper::toDto).toList();
        log.info("Fetched {} users", list.size());
        return list;
    }

    @Transactional(readOnly = true)
    public UserDto getUserByID(Long id) {
        User u =  userRepository.findById(id).orElseThrow(() -> new NotFoundException("User %d not found".formatted(id)));
        return toDto(u);
    }

    public UserDto createUser(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.email()))
            throw new ConflictException("Email already exists" + dto.email());

        User saved = userRepository.save(fromCreate(dto));
        log.info("Created user id = {} with email {}", saved.getId(), saved.getEmail());
        return toDto(saved);
    }

    public UserDto updateUser(Long id, UserUpdateDto dto) {
        User u = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User %d not found".formatted(id)));

        if (!u.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email()))
            throw new ConflictException("Email already exists" + dto.email());

        applyUpdate(dto, u);
        User saved = userRepository.save(u);
        log.info("Updated userid={}",  u.getId());
        return toDto(saved);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new NotFoundException("User %d not found".formatted(id));

        userRepository.deleteById(id);
        log.info("Deleted userid={}",  id);
    }
}

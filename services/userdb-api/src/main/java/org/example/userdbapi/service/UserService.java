package org.example.userdbapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userdbapi.dto.UserCreateDto;
import org.example.userdbapi.dto.UserDto;
import org.example.userdbapi.dto.UserUpdateDto;
import org.example.userdbapi.events.UserEvent;
import org.example.userdbapi.events.UserEventProducer;
import org.example.userdbapi.exception.ConflictException;
import org.example.userdbapi.exception.NotFoundException;
import org.example.userdbapi.mapper.UserMapper;
import org.example.userdbapi.model.User;
import org.example.userdbapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;                 // <— теперь бин, а не static-утилита
    private final UserEventProducer userEventProducer;   // <— продьюсер событий

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        var list = userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
        log.info("Fetched {} users", list.size());
        return list;
    }

    @Transactional(readOnly = true)
    public UserDto getUserByID(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User %d not found".formatted(id)));
        return userMapper.toDto(u);
    }

    public UserDto createUser(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("Email already exists: %s".formatted(dto.email()));
        }

        User saved = userRepository.save(userMapper.toEntity(dto));
        log.info("Created user id={}, email={}", saved.getId(), saved.getEmail());

        userEventProducer.send(UserEvent.created(
                saved.getId(),
                saved.getEmail(),
                "userdb-api"
        ));

        return userMapper.toDto(saved);
    }

    public UserDto updateUser(Long id, UserUpdateDto dto) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User %d not found".formatted(id)));

        if (!u.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("Email already exists: %s".formatted(dto.email()));
        }

        userMapper.applyUpdate(dto, u);
        User saved = userRepository.save(u);
        log.info("Updated userid={}", saved.getId());
        return userMapper.toDto(saved);
    }

    public void deleteUser(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User %d not found".formatted(id)));

        userRepository.deleteById(id);
        log.info("Deleted userid={}, email={}", id, u.getEmail());

        userEventProducer.send(UserEvent.deleted(
                id,
                u.getEmail(),
                "userdb-api"
        ));
    }
}
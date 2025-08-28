package org.example.userdbapi.mapper;

import lombok.extern.slf4j.Slf4j;
import org.example.userdbapi.dto.UserCreateDto;
import org.example.userdbapi.dto.UserDto;
import org.example.userdbapi.dto.UserUpdateDto;
import org.example.userdbapi.model.User;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserMapper {

    public UserDto toDto(User u) {
        if (u == null) return null;
        log.debug("Map entity -> dto: id={}, email={}", u.getId(), u.getEmail());
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getAge(), u.getCreatedAt());
    }

    /** было fromCreate(...) */
    public User toEntity(UserCreateDto d) {
        if (d == null) return null;
        User u = new User();
        u.setName(d.name());
        u.setEmail(d.email());
        u.setAge(d.age());
        log.debug("Map createDto -> entity: email={}", d.email());
        return u;
    }

    public void applyUpdate(UserUpdateDto d, User u) {
        if (d == null || u == null) return;
        u.setName(d.name());
        u.setEmail(d.email());
        u.setAge(d.age());
        log.debug("Apply updateDto -> entity: id={}, email={}", u.getId(), u.getEmail());
    }
}
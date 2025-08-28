package org.example.userdbapi.mapper;

import org.example.userdbapi.dto.UserCreateDto;
import org.example.userdbapi.dto.UserDto;
import org.example.userdbapi.dto.UserUpdateDto;
import org.example.userdbapi.model.User;

public final class UserMapper {
    private UserMapper() {}

    public static UserDto toDto(User u){
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getAge(), u.getCreatedAt());
    }

    public static User fromCreate(UserCreateDto d){
        User u = new User();
        u.setName(d.name());
        u.setEmail(d.email());
        u.setAge(d.age());
        return u;
    }

    public static void applyUpdate(UserUpdateDto d, User u){
        u.setName(d.name());
        u.setEmail(d.email());
        u.setAge(d.age());
    }
}

package org.example.userdbapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userdbapi.dto.UserCreateDto;
import org.example.userdbapi.dto.UserDto;
import org.example.userdbapi.dto.UserUpdateDto;
import org.example.userdbapi.hateoas.UserModelAssembler;
import org.example.userdbapi.service.UserService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserModelAssembler assembler;

    @GetMapping()
    public CollectionModel<EntityModel<UserDto>> getAll() {
        var models = userService.getAllUsers()
                .stream()
                .map(assembler::toModel)
                .toList();

        return CollectionModel.of(models,
                linkTo(methodOn(UserController.class).getAll()).withSelfRel(),
                linkTo(methodOn(UserController.class).create(null)).withRel("create"));
    }

    @GetMapping("/{id}")
    public EntityModel<UserDto> getById(@PathVariable("id") Long id) {
        return assembler.toModel(userService.getUserByID(id));
    }

    @PostMapping()
    public ResponseEntity<EntityModel<UserDto>> create(@Valid @RequestBody UserCreateDto dto) {
        var created = userService.createUser(dto);
        var model = assembler.toModel(created);
        URI location = model.getRequiredLink("self").toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserDto>> update(@PathVariable("id") Long id, @Valid @RequestBody UserUpdateDto dto) {
        var updated = userService.updateUser(id, dto);
        return ResponseEntity.ok(assembler.toModel(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

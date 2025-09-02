package org.example.userdbapi.hateoas;

import lombok.extern.slf4j.Slf4j;
import org.example.userdbapi.controller.UserController;
import org.example.userdbapi.dto.UserDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Component
public class UserModelAssembler implements RepresentationModelAssembler<UserDto, EntityModel<UserDto>> {

    @Override
    public EntityModel<UserDto> toModel(UserDto dto) {
        var model = EntityModel.of(dto,
                linkTo(methodOn(UserController.class).getById(dto.id())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAll()).withRel("all-users"),
                linkTo(methodOn(UserController.class).update(dto.id(), null)).withRel("update"),
                linkTo(methodOn(UserController.class).delete(dto.id())).withRel("delete"));

        log.debug("Built HATEOAS links for user: {}", dto.id());
        return model;
    }
}

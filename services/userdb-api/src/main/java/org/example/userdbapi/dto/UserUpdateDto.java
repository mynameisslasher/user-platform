package org.example.userdbapi.dto;

import jakarta.validation.constraints.*;

public record UserUpdateDto(
        @NotBlank @Size(max=255) String name,
        @NotBlank @Email @Size(max=255) String email,
        @NotNull @Min(0) @Max(150) Integer age
) {}

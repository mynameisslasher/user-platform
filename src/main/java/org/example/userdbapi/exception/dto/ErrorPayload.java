package org.example.userdbapi.exception.dto;

public record ErrorPayload(
        int status,
        String error,
        String message,
        String path
) {}

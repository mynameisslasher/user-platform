package org.example.userdbapi.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path
) {
    public static ErrorResponse badRequest(String msg, String path) {
        return new ErrorResponse(400, "Bad Request", msg, path);
    }
    public static ErrorResponse conflict(String msg, String path) {
        return new ErrorResponse(409, "Conflict", msg, path);
    }
    public static ErrorResponse unexpected(String msg, String path) {
        return new ErrorResponse(500, "Internal Server Error", msg, path);
    }
}

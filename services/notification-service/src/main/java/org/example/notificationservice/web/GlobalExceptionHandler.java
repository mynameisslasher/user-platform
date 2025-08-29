package org.example.notificationservice.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse build(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    // 400: ошибки валидации @Valid тела запроса
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> "%s: %s".formatted(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {} -> {}", req.getRequestURI(), msg);
        return build(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    // 400: ошибки валидации параметров (@Validated на методах/параметрах)
    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("Constraint violation: {} -> {}", req.getRequestURI(), msg);
        return build(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    // 400: битый JSON, несовпадение enum и т.п.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.warn("Bad request body: {} -> {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return build(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    // 502: проблемы при отправке письма (внешний SMTP)
    @ExceptionHandler(MailException.class)
    public ErrorResponse handleMail(MailException ex, HttpServletRequest req) {
        log.error("Mail send failed for {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.BAD_GATEWAY, "Mail gateway error: " + ex.getMessage(), req.getRequestURI());
    }

    // 400: явная ошибка клиента
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleIllegal(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("Illegal argument at {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
    }

    // 500: всё остальное
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", req.getRequestURI());
    }
}
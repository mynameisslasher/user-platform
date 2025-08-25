package org.example.userdbapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.userdbapi.exception.dto.ErrorPayload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - not found
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorPayload> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        var status = HttpStatus.NOT_FOUND;
        warn(req, ex.getMessage()); // твой хелпер
        return ResponseEntity.status(status)
                .body(new ErrorPayload(status.value(), status.getReasonPhrase(), ex.getMessage(), req.getRequestURI()));
    }

    // 409 - conflict
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorPayload> handleConflict(ConflictException ex, HttpServletRequest req) {
        var status = HttpStatus.CONFLICT;
        warn(req, ex.getMessage());
        return ResponseEntity.status(status)
                .body(new ErrorPayload(status.value(), status.getReasonPhrase(), ex.getMessage(), req.getRequestURI()));
    }

    // 400 - valid errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorPayload> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var status  = HttpStatus.BAD_REQUEST;
        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .distinct()
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        warn(req, message);
        return ResponseEntity.status(status)
                .body(new ErrorPayload(status.value(), status.getReasonPhrase(), message, req.getRequestURI()));
    }

    // 400 - bad JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorPayload> handleBadJson(HttpServletRequest req) {
        var status  = HttpStatus.BAD_REQUEST;
        var message = "Malformed JSON";

        warn(req, message);
        return ResponseEntity.status(status)
                .body(new ErrorPayload(status.value(), status.getReasonPhrase(), message, req.getRequestURI()));
    }

    // 500
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorPayload> handleUnexpected(Exception ex, HttpServletRequest req) {
//        var status  = HttpStatus.INTERNAL_SERVER_ERROR;
//        log.error("{} {} -> {}", req.getMethod(), req.getRequestURI(), ex.getMessage(), ex);
//        return ResponseEntity.status(status)
//                .body(new ErrorPayload(status.value(), status.getReasonPhrase(), "Unexpected Error", req.getRequestURI()));
//    }

    // helper
    private void warn(HttpServletRequest req, String msg) {
        log.warn("{} {} -> {}", req.getMethod(), req.getRequestURI(), msg);
    }
}

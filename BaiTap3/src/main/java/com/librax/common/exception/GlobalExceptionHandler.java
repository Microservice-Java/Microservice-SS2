package com.librax.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleBookServiceUnavailable(BookServiceUnavailableException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("error", "SERVICE_UNAVAILABLE");
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "INTERNAL_SERVER_ERROR");
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

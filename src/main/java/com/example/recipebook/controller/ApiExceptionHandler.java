package com.example.recipebook.controller;

import com.example.recipebook.service.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> badRequest(BadRequestException exception) {
        if (!exception.getFieldErrors().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", Map.of("fieldErrors", exception.getFieldErrors())));
        }
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generic(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", exception.getMessage() == null ? "Internal error" : exception.getMessage()));
    }

    public static ResponseEntity<Map<String, Object>> conflictUsedInDishes(List<String> dishNames) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Cannot delete product used in dishes",
                "dishes", dishNames.stream().map(name -> Map.of("name", name)).toList()
        ));
    }
}

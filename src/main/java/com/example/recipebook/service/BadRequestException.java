package com.example.recipebook.service;

import org.springframework.aop.BeforeAdvice;

import java.util.List;
import java.util.Map;

public class BadRequestException extends RuntimeException {
    private final Map<String, List<String>> fieldErrors;

    public BadRequestException(String message) {
        super(message);
        this.fieldErrors = Map.of();
    }

    public BadRequestException(Map<String, List<String>> fieldErrors) {
        super("Validation failed");
        this.fieldErrors = fieldErrors;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}

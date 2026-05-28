package com.example.recipebook.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Validation {
    private Validation() {
    }

    public static void add(Map<String, List<String>> errors, String field, String message) {
        errors.computeIfAbsent(field, ignored -> new ArrayList<>()).add(message);
    }

    public static Map<String, List<String>> errors() {
        return new LinkedHashMap<>();
    }

    public static void throwIfAny(Map<String, List<String>> errors) {
        if (!errors.isEmpty()) throw new BadRequestException(errors);
    }
}

package com.example.recipebook.controller;

import com.example.recipebook.model.Dish;
import com.example.recipebook.model.Flags;
import com.example.recipebook.model.Ingredient;
import com.example.recipebook.model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class FormMapper {
    private final ObjectMapper objectMapper;

    public FormMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Product product(
            String name,
            String photos,
            List<String> uploadedPhotos,
            double calories,
            double proteins,
            double fats,
            double carbs,
            String compositionText,
            String category,
            String cookingNeed,
            String flags
    ) {
        Product product = new Product();
        product.setName(name);
        product.setPhotos(mergePhotos(photos, uploadedPhotos));
        product.setCalories(calories);
        product.setProteins(proteins);
        product.setFats(fats);
        product.setCarbs(carbs);
        product.setCompositionText(compositionText == null || compositionText.isBlank() ? null : compositionText);
        product.setCategory(category);
        product.setCookingNeed(cookingNeed);
        product.setFlags(parse(flags, Flags.class, new Flags()));
        return product;
    }

    public Dish dish(
            String name,
            String photos,
            List<String> uploadedPhotos,
            Double calories,
            Double proteins,
            Double fats,
            Double carbs,
            String composition,
            double portionSize,
            String category,
            String flags
    ) {
        Dish dish = new Dish();
        dish.setName(name);
        dish.setPhotos(mergePhotos(photos, uploadedPhotos));
        dish.setCalories(calories == null ? 0 : calories);
        dish.setProteins(proteins == null ? 0 : proteins);
        dish.setFats(fats == null ? 0 : fats);
        dish.setCarbs(carbs == null ? 0 : carbs);
        dish.setComposition(parseList(composition, new TypeReference<List<Ingredient>>() {}));
        dish.setPortionSize(portionSize);
        dish.setCategory(category);
        dish.setFlags(parse(flags, Flags.class, new Flags()));
        return dish;
    }

    public boolean provided(Double value) {
        return value != null;
    }

    public List<MultipartFile> files(List<MultipartFile> files) {
        return files == null ? List.of() : files;
    }

    private List<String> mergePhotos(String existingJson, List<String> uploaded) {
        List<String> result = new ArrayList<>(parseList(existingJson, new TypeReference<List<String>>() {}));
        result.addAll(uploaded == null ? List.of() : uploaded);
        return result;
    }

    private <T> List<T> parseList(String json, TypeReference<List<T>> type) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private <T> T parse(String json, Class<T> type, T fallback) {
        if (json == null || json.isBlank()) return fallback;
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}

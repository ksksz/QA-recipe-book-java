package com.example.recipebook.controller;

import com.example.recipebook.dto.DishDetailsResponse;
import com.example.recipebook.model.Dish;
import com.example.recipebook.service.DishService;
import com.example.recipebook.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class DishController {
    private final DishService dishService;
    private final FileStorageService fileStorageService;
    private final FormMapper formMapper;

    public DishController(DishService dishService, FileStorageService fileStorageService, ObjectMapper objectMapper) {
        this.dishService = dishService;
        this.fileStorageService = fileStorageService;
        this.formMapper = new FormMapper(objectMapper);
    }

    @GetMapping("/api/dishes")
    public List<Dish> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam Map<String, String> params
    ) {
        return dishService.list(category, search, sort, params);
    }

    @GetMapping("/api/dishes/{id}")
    public DishDetailsResponse get(@PathVariable String id) {
        return dishService.getDetails(id);
    }

    @PostMapping("/api/dishes")
    public ResponseEntity<Dish> create(@ModelAttribute DishForm form) {
        Dish dish = map(form);
        return ResponseEntity.status(201).body(dishService.create(
                dish,
                form.calories() != null,
                form.proteins() != null,
                form.fats() != null,
                form.carbs() != null
        ));
    }

    @PutMapping("/api/dishes/{id}")
    public Dish update(@PathVariable String id, @ModelAttribute DishForm form) {
        Dish dish = map(form);
        return dishService.update(
                id,
                dish,
                form.calories() != null,
                form.proteins() != null,
                form.fats() != null,
                form.carbs() != null
        );
    }

    @DeleteMapping("/api/dishes/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        dishService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Dish map(DishForm form) {
        List<String> uploaded = fileStorageService.savePhotos(formMapper.files(form.photosFiles()));
        return formMapper.dish(
                form.name(),
                form.photos(),
                uploaded,
                form.calories(),
                form.proteins(),
                form.fats(),
                form.carbs(),
                form.composition(),
                form.portionSize() == null ? 0 : form.portionSize(),
                form.category(),
                form.flags()
        );
    }

    public record DishForm(
            String name,
            String photos,
            Double calories,
            Double proteins,
            Double fats,
            Double carbs,
            String composition,
            Double portionSize,
            String category,
            String flags,
            List<MultipartFile> photosFiles
    ) {
    }
}

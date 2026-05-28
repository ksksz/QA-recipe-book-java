package com.example.recipebook.controller;

import com.example.recipebook.model.Product;
import com.example.recipebook.service.FileStorageService;
import com.example.recipebook.service.ProductService;
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
public class ProductController {
    private final ProductService productService;
    private final FileStorageService fileStorageService;
    private final FormMapper formMapper;

    public ProductController(ProductService productService, FileStorageService fileStorageService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.fileStorageService = fileStorageService;
        this.formMapper = new FormMapper(objectMapper);
    }

    @GetMapping("/api/products")
    public List<Product> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String cookingNeed,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam Map<String, String> params
    ) {
        return productService.list(category, cookingNeed, search, sort, params);
    }

    @GetMapping("/api/products/{id}")
    public Product get(@PathVariable String id) {
        return productService.getById(id);
    }

    @PostMapping("/api/products")
    public ResponseEntity<Product> create(@ModelAttribute ProductForm form) {
        Product product = map(form);
        return ResponseEntity.status(201).body(productService.create(product));
    }

    @PutMapping("/api/products/{id}")
    public Product update(@PathVariable String id, @ModelAttribute ProductForm form) {
        return productService.update(id, map(form));
    }

    @DeleteMapping("/api/products/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        List<String> usedIn = productService.delete(id);
        if (!usedIn.isEmpty()) return ApiExceptionHandler.conflictUsedInDishes(usedIn);
        return ResponseEntity.noContent().build();
    }

    private Product map(ProductForm form) {
        List<String> uploaded = fileStorageService.savePhotos(formMapper.files(form.photosFiles()));
        return formMapper.product(
                form.name(),
                form.photos(),
                uploaded,
                number(form.calories()),
                number(form.proteins()),
                number(form.fats()),
                number(form.carbs()),
                form.compositionText(),
                form.category(),
                form.cookingNeed(),
                form.flags()
        );
    }

    private double number(Double value) {
        return value == null ? 0 : value;
    }

    public record ProductForm(
            String name,
            String photos,
            Double calories,
            Double proteins,
            Double fats,
            Double carbs,
            String compositionText,
            String category,
            String cookingNeed,
            String flags,
            List<MultipartFile> photosFiles
    ) {
    }
}

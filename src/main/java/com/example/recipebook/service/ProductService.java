package com.example.recipebook.service;

import com.example.recipebook.model.Flags;
import com.example.recipebook.model.Product;
import com.example.recipebook.storage.RecipeBookDatabase;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ProductService implements ProductLookup {
    private final RecipeBookDatabase recipeBookDatabase;

    public ProductService(RecipeBookDatabase recipeBookDatabase) {
        this.recipeBookDatabase = recipeBookDatabase;
    }

    public List<Product> list(String category, String cookingNeed, String search, String sort, Map<String, String> flags) {
        Stream<Product> stream = recipeBookDatabase.listProducts().stream();
        if (category != null && !category.isBlank()) stream = stream.filter(p -> category.equals(p.getCategory()));
        if (cookingNeed != null && !cookingNeed.isBlank()) stream = stream.filter(p -> cookingNeed.equals(p.getCookingNeed()));
        if (search != null && !search.isBlank()) {
            String needle = search.toLowerCase();
            stream = stream.filter(p -> p.getName().toLowerCase().contains(needle));
        }
        for (String flag : RecipeConstants.FLAGS) {
            if ("true".equals(flags.get(flag))) stream = stream.filter(p -> p.getFlags().getByKey(flag));
        }
        List<Product> result = stream.toList();
        if (sort == null || sort.isBlank()) return result;
        Comparator<Product> comparator = switch (sort) {
            case "name" -> Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
            case "calories" -> Comparator.comparingDouble(Product::getCalories);
            case "proteins" -> Comparator.comparingDouble(Product::getProteins);
            case "fats" -> Comparator.comparingDouble(Product::getFats);
            case "carbs" -> Comparator.comparingDouble(Product::getCarbs);
            default -> null;
        };
        return comparator == null ? result : result.stream().sorted(comparator).toList();
    }

    @Override
    public Product getById(String productId) {
        return recipeBookDatabase.findProduct(productId)
                .orElseThrow(() -> new BadRequestException("Product not found"));
    }

    public Product create(Product product) {
        validate(product);
        product.setId(UUID.randomUUID().toString());
        product.setName(product.getName().trim());
        product.setCreatedAt(Instant.now().toString());
        product.setUpdatedAt(null);
        recipeBookDatabase.insertProduct(product);
        return product;
    }

    public Product update(String id, Product product) {
        validate(product);
        Product existing = getById(id);
        product.setId(existing.getId());
        product.setName(product.getName().trim());
        product.setCreatedAt(existing.getCreatedAt());
        product.setUpdatedAt(Instant.now().toString());
        recipeBookDatabase.updateProduct(product);
        return product;
    }

    public List<String> delete(String id) {
        Product product = getById(id);
        List<String> usedIn = recipeBookDatabase.listDishes().stream()
                .filter(d -> d.getComposition().stream().anyMatch(row -> id.equals(row.getProductId())))
                .map(d -> d.getName())
                .toList();
        if (!usedIn.isEmpty()) return usedIn;
        recipeBookDatabase.deleteProduct(product.getId());
        return List.of();
    }

    private void validate(Product product) {
        Map<String, List<String>> errors = Validation.errors();
        if (product.getName() == null || product.getName().trim().length() < 2) {
            Validation.add(errors, "name", "Минимальная длина названия — 2 символа");
        }
        if (product.getPhotos().size() > RecipeConstants.MAX_PHOTOS) {
            Validation.add(errors, "photos", "Можно добавить не более 5 фото");
        }
        if (product.getCalories() < 0) Validation.add(errors, "calories", "Значение не может быть отрицательным");
        if (product.getProteins() < 0) Validation.add(errors, "proteins", "Значение не может быть отрицательным");
        if (product.getFats() < 0) Validation.add(errors, "fats", "Значение не может быть отрицательным");
        if (product.getCarbs() < 0) Validation.add(errors, "carbs", "Значение не может быть отрицательным");
        if (product.getProteins() > 100) Validation.add(errors, "proteins", "Значение не может превышать 100");
        if (product.getFats() > 100) Validation.add(errors, "fats", "Значение не может превышать 100");
        if (product.getCarbs() > 100) Validation.add(errors, "carbs", "Значение не может превышать 100");
        if (product.getProteins() + product.getFats() + product.getCarbs() > 100) {
            Validation.add(errors, "macrosTotal", "Сумма белков, жиров и углеводов не может превышать 100 г на 100 г продукта");
        }
        if (!RecipeConstants.PRODUCT_CATEGORIES.contains(product.getCategory())) Validation.add(errors, "category", "Некорректная категория");
        if (!RecipeConstants.COOKING_NEED.contains(product.getCookingNeed())) Validation.add(errors, "cookingNeed", "Некорректная готовность");
        if (product.getFlags() == null) product.setFlags(new Flags());
        Validation.throwIfAny(errors);
    }
}

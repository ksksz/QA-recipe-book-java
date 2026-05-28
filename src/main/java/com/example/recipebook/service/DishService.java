package com.example.recipebook.service;

import com.example.recipebook.dto.DishDetailsResponse;
import com.example.recipebook.model.Dish;
import com.example.recipebook.model.Flags;
import com.example.recipebook.model.Ingredient;
import com.example.recipebook.model.Nutrition;
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
public class DishService {
    private final RecipeBookDatabase recipeBookDatabase;
    private final ProductLookup productLookup;

    public DishService(RecipeBookDatabase recipeBookDatabase, ProductLookup productLookup) {
        this.recipeBookDatabase = recipeBookDatabase;
        this.productLookup = productLookup;
    }

    public List<Dish> list(String category, String search, String sort, Map<String, String> flags) {
        Stream<Dish> stream = recipeBookDatabase.listDishes().stream();
        if (category != null && !category.isBlank()) stream = stream.filter(d -> category.equals(d.getCategory()));
        if (search != null && !search.isBlank()) {
            String needle = search.toLowerCase();
            stream = stream.filter(d -> d.getName().toLowerCase().contains(needle));
        }
        for (String flag : RecipeConstants.FLAGS) {
            if ("true".equals(flags.get(flag))) stream = stream.filter(d -> d.getFlags().getByKey(flag));
        }
        List<Dish> result = stream.toList();
        if (sort == null || sort.isBlank()) return result;
        Comparator<Dish> comparator = switch (sort) {
            case "name" -> Comparator.comparing(Dish::getName, String.CASE_INSENSITIVE_ORDER);
            case "calories" -> Comparator.comparingDouble(Dish::getCalories);
            case "proteins" -> Comparator.comparingDouble(Dish::getProteins);
            case "fats" -> Comparator.comparingDouble(Dish::getFats);
            case "carbs" -> Comparator.comparingDouble(Dish::getCarbs);
            case "portionSize" -> Comparator.comparingDouble(Dish::getPortionSize);
            default -> null;
        };
        return comparator == null ? result : result.stream().sorted(comparator).toList();
    }

    public Dish getById(String id) {
        return recipeBookDatabase.findDish(id)
                .orElseThrow(() -> new BadRequestException("Dish not found"));
    }

    public DishDetailsResponse getDetails(String id) {
        Dish dish = getById(id);
        DishDetailsResponse response = copyDish(dish);
        response.setCompositionDetailed(dish.getComposition().stream().map(row -> {
            DishDetailsResponse.IngredientDetails details = new DishDetailsResponse.IngredientDetails();
            details.setProductId(row.getProductId());
            details.setAmount(row.getAmount());
            try {
                details.setProduct(productLookup.getById(row.getProductId()));
            } catch (BadRequestException ignored) {
                details.setProduct(null);
            }
            return details;
        }).toList());
        return response;
    }

    public Dish create(Dish dish, boolean caloriesProvided, boolean proteinsProvided, boolean fatsProvided, boolean carbsProvided) {
        validateBase(dish);
        Dish prepared = prepareDish(dish, caloriesProvided, proteinsProvided, fatsProvided, carbsProvided);
        prepared.setId(UUID.randomUUID().toString());
        prepared.setCreatedAt(Instant.now().toString());
        prepared.setUpdatedAt(null);
        recipeBookDatabase.insertDish(prepared);
        return prepared;
    }

    public Dish update(String id, Dish dish, boolean caloriesProvided, boolean proteinsProvided, boolean fatsProvided, boolean carbsProvided) {
        validateBase(dish);
        Dish existing = getById(id);
        Dish prepared = prepareDish(dish, caloriesProvided, proteinsProvided, fatsProvided, carbsProvided);
        prepared.setId(existing.getId());
        prepared.setCreatedAt(existing.getCreatedAt());
        prepared.setUpdatedAt(Instant.now().toString());
        recipeBookDatabase.updateDish(prepared);
        return prepared;
    }

    public void delete(String id) {
        Dish dish = getById(id);
        recipeBookDatabase.deleteDish(dish.getId());
    }

    public Nutrition calculateDraft(List<Ingredient> composition) {
        validateComposition(composition);
        Nutrition result = new Nutrition();
        for (Ingredient row : composition) {
            Product product = productLookup.getById(row.getProductId());
            double ratio = row.getAmount() / 100.0;
            result.setCalories(result.getCalories() + product.getCalories() * ratio);
            result.setProteins(result.getProteins() + product.getProteins() * ratio);
            result.setFats(result.getFats() + product.getFats() * ratio);
            result.setCarbs(result.getCarbs() + product.getCarbs() * ratio);
        }
        return result;
    }

    private Dish prepareDish(Dish dish, boolean caloriesProvided, boolean proteinsProvided, boolean fatsProvided, boolean carbsProvided) {
        Nutrition draft = calculateDraft(dish.getComposition());
        DishName parsedName = parseCategoryMacro(dish.getName());
        String category = dish.getCategory() == null || dish.getCategory().isBlank() ? parsedName.category() : dish.getCategory();
        if (category == null || !RecipeConstants.DISH_CATEGORIES.contains(category)) {
            throw new BadRequestException(Map.of("category", List.of("Категория обязательна")));
        }

        dish.setName(parsedName.name());
        dish.setCategory(category);
        dish.setNutritionDraft(draft);
        dish.setCalories(caloriesProvided ? dish.getCalories() : draft.getCalories());
        dish.setProteins(proteinsProvided ? dish.getProteins() : draft.getProteins());
        dish.setFats(fatsProvided ? dish.getFats() : draft.getFats());
        dish.setCarbs(carbsProvided ? dish.getCarbs() : draft.getCarbs());

        validateNutritionAgainstPortion(dish);
        Flags available = resolveAvailableFlags(dish.getComposition());
        Flags requested = dish.getFlags() == null ? new Flags() : dish.getFlags();
        Flags finalFlags = new Flags();
        for (String flag : RecipeConstants.FLAGS) {
            finalFlags.setByKey(flag, available.getByKey(flag) && requested.getByKey(flag));
        }
        dish.setAvailableFlags(available);
        dish.setFlags(finalFlags);
        return dish;
    }

    private void validateBase(Dish dish) {
        Map<String, List<String>> errors = Validation.errors();
        if (dish.getName() == null || dish.getName().trim().length() < 2) Validation.add(errors, "name", "Минимальная длина названия — 2 символа");
        if (dish.getPhotos().size() > RecipeConstants.MAX_PHOTOS) Validation.add(errors, "photos", "Можно добавить не более 5 фото");
        if (dish.getCalories() < 0) Validation.add(errors, "calories", "Значение не может быть отрицательным");
        if (dish.getProteins() < 0) Validation.add(errors, "proteins", "Значение не может быть отрицательным");
        if (dish.getFats() < 0) Validation.add(errors, "fats", "Значение не может быть отрицательным");
        if (dish.getCarbs() < 0) Validation.add(errors, "carbs", "Значение не может быть отрицательным");
        if (dish.getPortionSize() <= 0) Validation.add(errors, "portionSize", "Размер порции должен быть больше 0");
        Validation.throwIfAny(errors);
    }

    private void validateComposition(List<Ingredient> composition) {
        if (composition == null || composition.isEmpty()) {
            throw new BadRequestException(Map.of("composition", List.of("Состав не должен быть пустым")));
        }
        for (Ingredient row : composition) {
            if (row.getAmount() <= 0) throw new BadRequestException("Количество каждого продукта должно быть больше 0");
            productLookup.getById(row.getProductId());
        }
    }

    private void validateNutritionAgainstPortion(Dish dish) {
        Map<String, List<String>> errors = Validation.errors();
        if (dish.getProteins() > dish.getPortionSize()) Validation.add(errors, "proteins", "Значение не может превышать размер порции блюда");
        if (dish.getFats() > dish.getPortionSize()) Validation.add(errors, "fats", "Значение не может превышать размер порции блюда");
        if (dish.getCarbs() > dish.getPortionSize()) Validation.add(errors, "carbs", "Значение не может превышать размер порции блюда");
        if (dish.getProteins() + dish.getFats() + dish.getCarbs() > dish.getPortionSize()) {
            Validation.add(errors, "macrosTotal", "Сумма белков, жиров и углеводов не может превышать размер порции блюда");
        }
        Validation.throwIfAny(errors);
    }

    private Flags resolveAvailableFlags(List<Ingredient> composition) {
        Flags result = new Flags();
        for (String flag : RecipeConstants.FLAGS) {
            boolean available = !composition.isEmpty() && composition.stream()
                    .map(row -> productLookup.getById(row.getProductId()))
                    .allMatch(product -> product.getFlags().getByKey(flag));
            result.setByKey(flag, available);
        }
        return result;
    }

    private DishName parseCategoryMacro(String rawName) {
        String lower = rawName.toLowerCase();
        int bestIndex = Integer.MAX_VALUE;
        String bestMacro = null;
        for (String macro : RecipeConstants.MACRO_TO_CATEGORY.keySet()) {
            int index = lower.indexOf(macro);
            if (index >= 0 && index < bestIndex) {
                bestIndex = index;
                bestMacro = macro;
            }
        }
        String cleaned = rawName;
        for (String macro : RecipeConstants.MACRO_TO_CATEGORY.keySet()) {
            cleaned = cleaned.replaceAll("(?i)\\s*" + macro + "\\s*", " ");
        }
        cleaned = cleaned.replaceAll("\\s{2,}", " ").trim();
        return new DishName(cleaned, bestMacro == null ? null : RecipeConstants.MACRO_TO_CATEGORY.get(bestMacro));
    }

    private DishDetailsResponse copyDish(Dish dish) {
        DishDetailsResponse response = new DishDetailsResponse();
        response.setId(dish.getId());
        response.setName(dish.getName());
        response.setPhotos(dish.getPhotos());
        response.setCalories(dish.getCalories());
        response.setProteins(dish.getProteins());
        response.setFats(dish.getFats());
        response.setCarbs(dish.getCarbs());
        response.setNutritionDraft(dish.getNutritionDraft());
        response.setComposition(dish.getComposition());
        response.setPortionSize(dish.getPortionSize());
        response.setCategory(dish.getCategory());
        response.setFlags(dish.getFlags());
        response.setAvailableFlags(dish.getAvailableFlags());
        response.setCreatedAt(dish.getCreatedAt());
        response.setUpdatedAt(dish.getUpdatedAt());
        return response;
    }

    private record DishName(String name, String category) {
    }
}

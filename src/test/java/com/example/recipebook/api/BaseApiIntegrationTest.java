package com.example.recipebook.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ResourceLock("recipe-book-database")
@DisplayName("Базовая настройка интеграционных API-тестов")
abstract class BaseApiIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    protected ResultActions createProduct(
            String name,
            String calories,
            String proteins,
            String fats,
            String carbs,
            String category,
            String cookingNeed
    ) throws Exception {
        return createProductWithFlags(name, calories, proteins, fats, carbs, category, cookingNeed, flags(true, true, true));
    }

    protected ResultActions createProductWithFlags(
            String name,
            String calories,
            String proteins,
            String fats,
            String carbs,
            String category,
            String cookingNeed,
            String flags
    ) throws Exception {
        return mockMvc.perform(post("/api/products")
                .param("name", name)
                .param("calories", calories)
                .param("proteins", proteins)
                .param("fats", fats)
                .param("carbs", carbs)
                .param("category", category)
                .param("cookingNeed", cookingNeed)
                .param("flags", flags));
    }

    protected ResultActions updateProduct(
            String id,
            String name,
            String calories,
            String proteins,
            String fats,
            String carbs,
            String category,
            String cookingNeed
    ) throws Exception {
        return mockMvc.perform(put("/api/products/{id}", id)
                .param("name", name)
                .param("calories", calories)
                .param("proteins", proteins)
                .param("fats", fats)
                .param("carbs", carbs)
                .param("category", category)
                .param("cookingNeed", cookingNeed)
                .param("flags", flags(true, true, true)));
    }

    protected ResultActions createDish(
            String name,
            String composition,
            String portionSize,
            String category
    ) throws Exception {
        return createDishWithNutrition(name, composition, portionSize, category, null, null, null, null);
    }

    protected ResultActions createDishWithNutrition(
            String name,
            String composition,
            String portionSize,
            String category,
            String calories,
            String proteins,
            String fats,
            String carbs
    ) throws Exception {
        var builder = post("/api/dishes")
                .param("name", name)
                .param("composition", composition)
                .param("portionSize", portionSize)
                .param("category", category)
                .param("flags", flags(true, true, true));
        if (calories != null) builder.param("calories", calories);
        if (proteins != null) builder.param("proteins", proteins);
        if (fats != null) builder.param("fats", fats);
        if (carbs != null) builder.param("carbs", carbs);
        return mockMvc.perform(builder);
    }

    protected ResultActions updateDish(
            String id,
            String name,
            String composition,
            String portionSize,
            String category
    ) throws Exception {
        return mockMvc.perform(put("/api/dishes/{id}", id)
                .param("name", name)
                .param("composition", composition)
                .param("portionSize", portionSize)
                .param("category", category)
                .param("flags", flags(true, true, true)));
    }

    protected String createdProductId(
            String name,
            String calories,
            String proteins,
            String fats,
            String carbs,
            String category,
            String cookingNeed
    ) throws Exception {
        MvcResult result = createProduct(name, calories, proteins, fats, carbs, category, cookingNeed)
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    protected String createdProductIdWithFlags(
            String name,
            String calories,
            String proteins,
            String fats,
            String carbs,
            String category,
            String cookingNeed,
            String flags
    ) throws Exception {
        MvcResult result = createProductWithFlags(name, calories, proteins, fats, carbs, category, cookingNeed, flags)
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    protected String createdDishId(String name, String composition, String portionSize, String category) throws Exception {
        MvcResult result = createDish(name, composition, portionSize, category)
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    protected String composition(String productId, String amount) throws Exception {
        JsonNode node = objectMapper.readTree("""
                [
                  {"productId":"%s","amount":%s}
                ]
                """.formatted(productId, amount));
        return objectMapper.writeValueAsString(node);
    }

    protected String composition(String firstProductId, String firstAmount, String secondProductId, String secondAmount) throws Exception {
        JsonNode node = objectMapper.readTree("""
                [
                  {"productId":"%s","amount":%s},
                  {"productId":"%s","amount":%s}
                ]
                """.formatted(firstProductId, firstAmount, secondProductId, secondAmount));
        return objectMapper.writeValueAsString(node);
    }

    protected List<String> textValues(JsonNode array, String field) {
        List<String> values = new ArrayList<>();
        array.forEach(node -> values.add(node.get(field).asText()));
        return values;
    }

    protected int indexOfText(JsonNode array, String field, String value) {
        List<String> values = textValues(array, field);
        return values.indexOf(value);
    }

    protected String flags(boolean vegan, boolean glutenFree, boolean sugarFree) {
        return """
                {"vegan":%s,"glutenFree":%s,"sugarFree":%s}
                """.formatted(vegan, glutenFree, sugarFree);
    }
}

package com.example.recipebook.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// -------------------
// Списки
// -------------------

@DisplayName("API справочников")
class MetaApiIntegrationTest extends BaseApiIntegrationTest {
    /**
     * Справочник продуктов должен содержать все допустимые категории,
     * которые используются в позитивных и негативных CRUDах
     */
    @Test
    @DisplayName("Возвращает категории продуктов")
    void shouldReturnProductCategories() throws Exception {
        mockMvc.perform(get("/api/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PRODUCT_CATEGORIES", hasSize(9)))
                .andExpect(jsonPath("$.PRODUCT_CATEGORIES", hasItems("Овощи", "Мясной", "Сладости")));
    }

    /**
     * Справочник готовности продуктов
     */
    @Test
    @DisplayName("Возвращает варианты готовности продуктов")
    void shouldReturnCookingNeedValues() throws Exception {
        mockMvc.perform(get("/api/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.COOKING_NEED", containsInAnyOrder(
                        "Готовый к употреблению",
                        "Полуфабрикат",
                        "Требует приготовления"
                )));
    }

    /**
     * Справочник категорий блюд должен совпадать с категориями,
     * которые принимает Dish CRUD API
     */
    @Test
    @DisplayName("Возвращает категории блюд")
    void shouldReturnDishCategories() throws Exception {
        mockMvc.perform(get("/api/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DISH_CATEGORIES", hasSize(7)))
                .andExpect(jsonPath("$.DISH_CATEGORIES", hasItems("Суп", "Второе", "Десерт")));
    }

    /**
     * Справочник флагов используется фильтрами продуктов и блюд
     */
    @Test
    @DisplayName("Возвращает доступные флаги")
    void shouldReturnFlags() throws Exception {
        mockMvc.perform(get("/api/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.FLAGS", containsInAnyOrder("vegan", "glutenFree", "sugarFree")));
    }
}

// ------------
// Продукты
// ------------

@DisplayName("API продуктов")
class ProductApiIntegrationTest extends BaseApiIntegrationTest {
    /**
     * ЭР Корректные справочники, неотрицательные КБЖУ
     * и сумма нутриентов не больше 100 г образуют валидный класс
     */
    @Test
    @DisplayName("Создаёт продукт из валидного класса данных")
    void shouldCreateProductFromValidClass() throws Exception {
        createProduct("Картофель", "77", "2", "0.4", "16.3", "Овощи", "Требует приготовления")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Картофель"))
                .andExpect(jsonPath("$.category").value("Овощи"))
                .andExpect(jsonPath("$.cookingNeed").value("Требует приготовления"))
                .andExpect(jsonPath("$.calories").value(77.0));
    }

    /**
     * ЭР Категория вне разрешённого справочника
     * принадлежит невалидному классу и отклоняется на уровне API
     */
    @Test
    @DisplayName("Отклоняет продукт с некорректной категорией")
    void shouldRejectProductWithUnknownCategory() throws Exception {
        createProduct("Картофель", "77", "2", "0.4", "16.3", "Фрукты", "Требует приготовления")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors.category[0]").value("Некорректная категория"));
    }

    /**
     * ЭР Сумма белков, жиров и углеводов больше 100 г
     * физически невозможна для 100 г продукта
     */
    @Test
    @DisplayName("Отклоняет невозможную сумму нутриентов")
    void shouldRejectProductWithImpossibleMacrosTotal() throws Exception {
        createProduct("Смесь", "120", "60", "30", "20", "Крупы", "Полуфабрикат")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors.macrosTotal[0]")
                        .value("Сумма белков, жиров и углеводов не может превышать 100 г на 100 г продукта"));
    }

    /**
     * ЭР Валидные классы для поиска, фильтра и сортировки
     * должны вернуть только продукты подходящей категории в нужном порядке
     */
    @Test
    @DisplayName("Фильтрует и сортирует список продуктов")
    void shouldFilterAndSortProducts() throws Exception {
        String token = "говtoken";
        String beefName = "Говядина " + token;
        createProduct("Вода", "0", "0", "0", "0", "Жидкость", "Готовый к употреблению")
                .andExpect(status().isCreated());
        createProduct(beefName, "187.2", "18.9", "12.4", "0", "Мясной", "Требует приготовления")
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/api/products")
                        .param("category", "Мясной")
                        .param("search", token)
                        .param("sort", "calories"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode products = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(textValues(products, "name")).contains(beefName);
    }

    /**
     * Проверяем значения ниже нижней границы,
     * на границе и выше границы для калорийности
     */
    @ParameterizedTest(name = "calories={0}, status={1}")
    @CsvSource({
            "-0.1, 400",
            "0, 201",
            "0.1, 201"
    })
    @DisplayName("Проверяет нижнюю границу калорийности")
    void shouldValidateCaloriesLowerBoundary(String calories, int expectedStatus) throws Exception {
        createProduct("Граничный продукт", calories, "0", "0", "0", "Овощи", "Готовый к употреблению")
                .andExpect(status().is(expectedStatus));
    }

    /**
     * Длина названия меньше минимума,
     * ровно на минимуме и выше минимума.
     */
    @ParameterizedTest(name = "name={0}, status={1}")
    @CsvSource({
            "A, 400",
            "Ab, 201",
            "Aba, 201"
    })
    @DisplayName("Проверяет нижнюю границу длины названия")
    void shouldValidateProductNameLengthBoundary(String name, int expectedStatus) throws Exception {
        createProduct(name, "10", "1", "1", "1", "Овощи", "Готовый к употреблению")
                .andExpect(status().is(expectedStatus));
    }

    @ParameterizedTest(name = "category={0}")
    @CsvSource({
            "Замороженный",
            "Мясной",
            "Овощи",
            "Зелень",
            "Специи",
            "Крупы",
            "Консервы",
            "Жидкость",
            "Сладости"
    })
    @DisplayName("CRUD: создаёт продукты допустимых категорий")
    void shouldCreateProductsForEveryCategory(String category) throws Exception {
        String name = "Продукт категории " + category;

        createProduct(name, "45", "3", "2", "4", category, "Готовый к употреблению")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.category").value(category));
    }

    @ParameterizedTest(name = "cookingNeed={0}")
    @CsvSource({
            "Готовый к употреблению",
            "Полуфабрикат",
            "Требует приготовления"
    })
    @DisplayName("CRUD: создаёт продукты с допустимой готовностью")
    void shouldCreateProductsForEveryCookingNeed(String cookingNeed) throws Exception {
        String name = "Продукт готовности " + cookingNeed;

        createProduct(name, "55", "4", "2", "5", "Овощи", cookingNeed)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cookingNeed").value(cookingNeed));
    }

    @ParameterizedTest(name = "calories={0}, proteins={1}, fats={2}, carbs={3}")
    @CsvSource({
            "77, 2, 0.4, 16.3"
    })
    @DisplayName("CRUD: создаёт продукты с валидными КБЖУ")
    void shouldCreateProductsWithValidNutrition(String calories, String proteins, String fats, String carbs) throws Exception {
        String name = "Продукт КБЖУ";

        createProduct(name, calories, proteins, fats, carbs, "Крупы", "Полуфабрикат")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calories").value(Double.parseDouble(calories)))
                .andExpect(jsonPath("$.proteins").value(Double.parseDouble(proteins)))
                .andExpect(jsonPath("$.fats").value(Double.parseDouble(fats)))
                .andExpect(jsonPath("$.carbs").value(Double.parseDouble(carbs)));
    }

    @ParameterizedTest(name = "name={0}, calories={1}, proteins={2}, fats={3}, carbs={4}, category={5}, cookingNeed={6}")
    @CsvSource({
            "TooMuchTotal, 10, 40, 40, 30, Овощи, Готовый к употреблению"
    })
    @DisplayName("CRUD: отклоняет невалидные продукты")
    void shouldRejectInvalidProducts(String name, String calories, String proteins, String fats, String carbs, String category, String cookingNeed) throws Exception {
        createProduct(name, calories, proteins, fats, carbs, category, cookingNeed)
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "updated category={5}")
    @CsvSource({
            "33, 3, 3, 3, Крупы, Полуфабрикат"
    })
    @DisplayName("CRUD: обновляет продукты через API")
    void shouldUpdateProducts(String calories, String proteins, String fats, String carbs, String category, String cookingNeed) throws Exception {
        String id = createdProductId("Продукт до обновления", "10", "1", "1", "1", "Овощи", "Готовый к употреблению");
        String updatedName = "Продукт после обновления";

        updateProduct(id, updatedName, calories, proteins, fats, carbs, category, cookingNeed)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(updatedName))
                .andExpect(jsonPath("$.category").value(category));
    }

    @ParameterizedTest(name = "delete product {0}")
    @CsvSource({
            "1"
    })
    @DisplayName("CRUD: удаляет неиспользуемые продукты")
    void shouldDeleteUnusedProducts(String index) throws Exception {
        String id = createdProductId("Удаляемый продукт " + index, "20", "2", "2", "2", "Овощи", "Готовый к употреблению");

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isBadRequest());
    }

    /**
     * Созданный продукт должен читаться по id со всеми полями
     */
    @Test
    @DisplayName("CRUD: читает продукт по id")
    void shouldReadProductById() throws Exception {
        String name = "Продукт для чтения";
        String id = createdProductId(name, "101", "11", "5", "12", "Овощи", "Готовый к употреблению");

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.calories").value(101.0));
    }

    /**
     * Несуществующий id относится к невалидному классу чтения
     */
    @Test
    @DisplayName("CRUD: возвращает ошибку для несуществующего продукта")
    void shouldReturnBadRequestForMissingProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}", "missing-product-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Product not found"));
    }

    /**
     * Несуществующий id нельзя обновить
     */
    @Test
    @DisplayName("CRUD: возвращает ошибку при обновлении несуществующего продукта")
    void shouldReturnBadRequestWhenUpdatingMissingProduct() throws Exception {
        updateProduct("missing-product-id", "Нет продукта", "10", "1", "1", "1", "Овощи", "Готовый к употреблению")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Product not found"));
    }

    /**
     * Несуществующий id нельзя удалить.
     */
    @Test
    @DisplayName("CRUD: возвращает ошибку при удалении несуществующего продукта")
    void shouldReturnBadRequestWhenDeletingMissingProduct() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", "missing-product-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Product not found"));
    }

    /**
     * Фильтрация: API должен учитывать состояние
     */
    @Test
    @DisplayName("Фильтрует продукты по готовности")
    void shouldFilterProductsByCookingNeed() throws Exception {
        String token = "готовность";
        String readyName = "Готовый " + token;
        createProduct(readyName, "10", "1", "1", "1", "Овощи", "Готовый к употреблению")
                .andExpect(status().isCreated());
        createProduct("Полуфабрикат " + token, "20", "2", "2", "2", "Овощи", "Полуфабрикат")
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/api/products")
                        .param("search", token)
                        .param("cookingNeed", "Готовый к употреблению"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode products = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(textValues(products, "name"))
                .contains(readyName)
                .doesNotContain("Полуфабрикат " + token);
    }

    /**
     * Фильтрация: флаг vegan=true оставляет только подходящее
     */
    @Test
    @DisplayName("Фильтрует продукты по флагам")
    void shouldFilterProductsByFlags() throws Exception {
        String token = "флаг";
        String veganName = "Веган " + token;
        createProductWithFlags(veganName, "10", "1", "1", "1", "Овощи", "Готовый к употреблению", flags(true, true, true))
                .andExpect(status().isCreated());
        createProductWithFlags("Не веган " + token, "10", "1", "1", "1", "Овощи", "Готовый к употреблению", flags(false, true, true))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/api/products")
                        .param("search", token)
                        .param("vegan", "true"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode products = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(textValues(products, "name"))
                .contains(veganName)
                .doesNotContain("Не веган " + token);
    }

    /**
     * Сортировка по алфавиту
     */
    @Test
    @DisplayName("Сортирует продукты по названию")
    void shouldSortProductsByName() throws Exception {
        String token = "sort";
        String bName = "Бета " + token;
        String aName = "Альфа " + token;
        createProduct(bName, "20", "2", "2", "2", "Овощи", "Готовый к употреблению")
                .andExpect(status().isCreated());
        createProduct(aName, "10", "1", "1", "1", "Овощи", "Готовый к употреблению")
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/api/products")
                        .param("search", token)
                        .param("sort", "name"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode products = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(indexOfText(products, "name", aName)).isNotNegative();
        assertThat(indexOfText(products, "name", bName)).isGreaterThan(indexOfText(products, "name", aName));
    }

    /**
     * ЭР Неизвестная готовность продукта
     * относится к невалидному классу данных
     */
    @Test
    @DisplayName("Отклоняет продукт с некорректной готовностью")
    void shouldRejectProductWithUnknownCookingNeed() throws Exception {
        createProduct("Неизвестная готовность", "10", "1", "1", "1", "Овощи", "Сырой")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors.cookingNeed[0]").value("Некорректная готовность"));
    }

    /**
     * Отдельный макронутриент может быть равен 100,
     * но значение сразу выше 100 должно отклоняться
     */
    @ParameterizedTest(name = "{0}={1}, status={2}")
    @CsvSource({
            "proteins, 100, 201",
            "proteins, 100.1, 400",
            "fats, 100, 201",
            "fats, 100.1, 400",
            "carbs, 100, 201",
            "carbs, 100.1, 400"
    })
    @DisplayName("Проверяет верхнюю границу макронутриентов")
    void shouldValidateMacroUpperBoundary(String macro, String value, int expectedStatus) throws Exception {
        String proteins = "proteins".equals(macro) ? value : "0";
        String fats = "fats".equals(macro) ? value : "0";
        String carbs = "carbs".equals(macro) ? value : "0";

        createProduct("Верхняя граница " + macro, "100", proteins, fats, carbs, "Овощи", "Готовый к употреблению")
                .andExpect(status().is(expectedStatus));
    }

    /**
     * Сумма КБЖУ ровно 100 допустима,
     * а 100.1 нет
     */
    @ParameterizedTest(name = "proteins={0}, fats={1}, carbs={2}, status={3}")
    @CsvSource({
            "50, 25, 25, 201",
            "50, 25, 25.1, 400"
    })
    @DisplayName("Проверяет верхнюю границу суммы макронутриентов")
    void shouldValidateMacroTotalUpperBoundary(String proteins, String fats, String carbs, int expectedStatus) throws Exception {
        createProduct("Сумма нутриентов", "100", proteins, fats, carbs, "Овощи", "Готовый к употреблению")
                .andExpect(status().is(expectedStatus));
    }

    /**
     * ЭР Каждый отрицательный макронутриент невалиден
     */
    @ParameterizedTest(name = "negative field={0}")
    @ValueSource(strings = {"proteins", "fats", "carbs"})
    @DisplayName("Отклоняет отрицательные макронутриенты")
    void shouldRejectNegativeMacros(String macro) throws Exception {
        String proteins = "proteins".equals(macro) ? "-0.1" : "1";
        String fats = "fats".equals(macro) ? "-0.1" : "1";
        String carbs = "carbs".equals(macro) ? "-0.1" : "1";

        createProduct("Отрицательный " + macro, "10", proteins, fats, carbs, "Овощи", "Готовый к употреблению")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors." + macro + "[0]").value("Значение не может быть отрицательным"));
    }

    /**
     * К продукту можно приложить не больше пяти фото
     */
    @Test
    @DisplayName("Отклоняет продукт с количеством фото больше лимита")
    void shouldRejectProductWithTooManyPhotos() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/products")
                        .param("name", "Фото лимит")
                        .param("photos", "[\"1.png\",\"2.png\",\"3.png\",\"4.png\",\"5.png\",\"6.png\"]")
                        .param("calories", "10")
                        .param("proteins", "1")
                        .param("fats", "1")
                        .param("carbs", "1")
                        .param("category", "Овощи")
                        .param("cookingNeed", "Готовый к употреблению")
                        .param("flags", flags(true, true, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors.photos[0]").value("Можно добавить не более 5 фото"));
    }
}

// ----------------
// Расчёт и валидация блюд
// -----------------

@DisplayName("API расчёта и валидации блюд")
class DishCalculationApiIntegrationTest extends BaseApiIntegrationTest {
    /**
     * ЭР Валидное блюдо с существующими продуктами
     * рассчитывает черновые и итоговые КБЖУ без ручного ввода
     */
    @Test
    @DisplayName("Создаёт блюдо и рассчитывает пищевую ценность")
    void shouldCreateDishAndCalculateNutrition() throws Exception {
        String potatoId = createdProductId("Картофель", "77", "2", "0.4", "16.3", "Овощи", "Требует приготовления");
        String waterId = createdProductId("Вода", "0", "0", "0", "0", "Жидкость", "Готовый к употреблению");

        createDish("!суп Борщ", composition(potatoId, "100.0", waterId, "200.0"), "300", "")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Борщ"))
                .andExpect(jsonPath("$.category").value("Суп"))
                .andExpect(jsonPath("$.calories").value(77.0))
                .andExpect(jsonPath("$.nutritionDraft.calories").value(77.0));
    }

    /**
     * ЭР Состав с несуществующим продуктом
     * принадлежит невалидному классу и отклоняется без создания блюда
     */
    @Test
    @DisplayName("Отклоняет блюдо с неизвестным продуктом")
    void shouldRejectDishWithUnknownProduct() throws Exception {
        createDish("Суп", composition("missing-product", "100.0"), "300", "Суп")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Product not found"));
    }

    /**
     * ЭР Детали блюда раскрывают продукты состава
     * и позволяют проверить связь расчёта с исходными продуктами
     */
    @Test
    @DisplayName("Возвращает детали блюда с продуктами состава")
    void shouldReturnDishDetailsWithCompositionProducts() throws Exception {
        String potatoId = createdProductId("Картофель", "77", "2", "0.4", "16.3", "Овощи", "Требует приготовления");
        String dishId = createdDishId("Пюре", composition(potatoId, "100.0"), "250", "Второе");

        mockMvc.perform(get("/api/dishes/{id}", dishId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compositionDetailed", hasSize(1)))
                .andExpect(jsonPath("$.compositionDetailed[0].product.name").value("Картофель"));
    }

    /**
     * Количество ингредиента ниже границы,
     * на границе и сразу выше неё
     */
    @ParameterizedTest(name = "amount={0}, status={1}")
    @CsvSource({
            "-0.1, 400",
            "0, 400",
            "0.1, 201"
    })
    @DisplayName("Проверяет нижнюю границу количества ингредиента")
    void shouldValidateIngredientAmountLowerBoundary(String amount, int expectedStatus) throws Exception {
        String potatoId = createdProductId("Картофель", "77", "2", "0.4", "16.3", "Овощи", "Требует приготовления");

        createDish("Пюре", composition(potatoId, amount), "250", "Второе")
                .andExpect(status().is(expectedStatus));
    }

    /**
     * Размер порции ниже границы,
     * на границе и сразу выше
     */
    @ParameterizedTest(name = "portionSize={0}, status={1}")
    @CsvSource({
            "-0.1, 400",
            "0, 400",
            "0.1, 201"
    })
    @DisplayName("Проверяет нижнюю границу размера порции")
    void shouldValidatePortionSizeLowerBoundary(String portionSize, int expectedStatus) throws Exception {
        String potatoId = createdProductId("Картофель", "77", "2", "0.4", "16.3", "Овощи", "Требует приготовления");

        createDish("Пюре", composition(potatoId, "0.1"), portionSize, "Второе")
                .andExpect(status().is(expectedStatus));
    }

    @ParameterizedTest(name = "single ingredient amount={5}")
    @CsvSource({
            "10, 1, 2, 3, 50, 5, 0.5, 1, 1.5",
            "20, 2, 3, 4, 100, 20, 2, 3, 4",
            "180, 18, 19, 20, 1, 1.8, 0.18, 0.19, 0.2"
    })
    @DisplayName("Расчёт КБЖУ блюда с одним ингредиентом")
    void shouldCalculateSingleIngredientNutrition(String calories, String proteins, String fats, String carbs, String amount,
                                                   double expectedCalories, double expectedProteins, double expectedFats, double expectedCarbs) throws Exception {
        String productId = createdProductId("Ингредиент", calories, proteins, fats, carbs, "Овощи", "Готовый к употреблению");

        createDish("Блюдо один ингредиент", composition(productId, amount), "1000", "Второе")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calories", closeTo(expectedCalories, 0.0001)))
                .andExpect(jsonPath("$.proteins", closeTo(expectedProteins, 0.0001)))
                .andExpect(jsonPath("$.fats", closeTo(expectedFats, 0.0001)))
                .andExpect(jsonPath("$.carbs", closeTo(expectedCarbs, 0.0001)))
                .andExpect(jsonPath("$.nutritionDraft.calories", closeTo(expectedCalories, 0.0001)));
    }

    @ParameterizedTest(name = "two ingredients case={0}")
    @CsvSource({
            "1, 10, 1, 1, 1, 100, 20, 2, 2, 2, 100, 30, 3, 3, 3",
            "16, 200, 20, 10, 5, 5, 100, 10, 5, 2, 5, 15, 1.5, 0.75, 0.35"
    })
    @DisplayName("Расчёт суммы КБЖУ блюда из двух ингредиентов")
    void shouldCalculateTwoIngredientNutrition(String ignoredCase,
                                                String c1, String p1, String f1, String cb1, String amount1,
                                                String c2, String p2, String f2, String cb2, String amount2,
                                                double expectedCalories, double expectedProteins, double expectedFats, double expectedCarbs) throws Exception {
        String firstProductId = createdProductId("Первый ингредиент", c1, p1, f1, cb1, "Овощи", "Готовый к употреблению");
        String secondProductId = createdProductId("Второй ингредиент", c2, p2, f2, cb2, "Крупы", "Полуфабрикат");

        createDish("Блюдо два ингредиента", composition(firstProductId, amount1, secondProductId, amount2), "1000", "Второе")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calories", closeTo(expectedCalories, 0.0001)))
                .andExpect(jsonPath("$.proteins", closeTo(expectedProteins, 0.0001)))
                .andExpect(jsonPath("$.fats", closeTo(expectedFats, 0.0001)))
                .andExpect(jsonPath("$.carbs", closeTo(expectedCarbs, 0.0001)));
    }

    @ParameterizedTest(name = "manual override case={0}")
    @CsvSource({
            "8, 80, 8, 8, 8"
    })
    @DisplayName("Сохраняет ручные КБЖУ отдельно от чернового расчёта")
    void shouldUseManualNutritionOverrides(String ignoredCase, String calories, String proteins, String fats, String carbs) throws Exception {
        String productId = createdProductId("Ингредиент для ручного КБЖУ", "100", "10", "10", "10", "Овощи", "Готовый к употреблению");

        createDishWithNutrition("Блюдо ручное КБЖУ", composition(productId, "100"), "500", "Второе", calories, proteins, fats, carbs)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calories").value(Double.parseDouble(calories)))
                .andExpect(jsonPath("$.proteins").value(Double.parseDouble(proteins)))
                .andExpect(jsonPath("$.fats").value(Double.parseDouble(fats)))
                .andExpect(jsonPath("$.carbs").value(Double.parseDouble(carbs)))
                .andExpect(jsonPath("$.nutritionDraft.calories").value(100.0));
    }

    @ParameterizedTest(name = "details draft case={0}")
    @CsvSource({
            "8, 33, 3, 2, 1, 125"
    })
    @DisplayName("Возвращает рассчитанное КБЖУ в деталях блюда")
    void shouldReturnCalculatedNutritionInDishDetails(String ignoredCase, String calories, String proteins, String fats, String carbs, String amount) throws Exception {
        double expectedCalories = Double.parseDouble(calories) * Double.parseDouble(amount) / 100.0;
        String productId = createdProductId("Ингредиент деталей", calories, proteins, fats, carbs, "Овощи", "Готовый к употреблению");
        String dishId = createdDishId("Блюдо детали КБЖУ", composition(productId, amount), "1000", "Второе");

        mockMvc.perform(get("/api/dishes/{id}", dishId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nutritionDraft.calories", closeTo(expectedCalories, 0.0001)))
                .andExpect(jsonPath("$.compositionDetailed", hasSize(1)));
    }

    /**
     * ЭР Пустой состав блюда невалиден,
     * потому что расчёт КБЖУ невозможен
     */
    @Test
    @DisplayName("Отклоняет блюдо с пустым составом")
    void shouldRejectDishWithEmptyComposition() throws Exception {
        createDish("Пустое блюдо", "[]", "100", "Второе")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors.composition[0]").value("Состав не должен быть пустым"));
    }

    /**
     * ЭР Ручные отрицательные значения КБЖУ невалидны
     */
    @ParameterizedTest(name = "negative field={0}")
    @ValueSource(strings = {"calories", "proteins", "fats", "carbs"})
    @DisplayName("Отклоняет отрицательные ручные КБЖУ блюда")
    void shouldRejectNegativeManualNutrition(String field) throws Exception {
        String productId = createdProductId("Ингредиент ручной валидации", "10", "1", "1", "1", "Овощи", "Готовый к употреблению");
        String calories = "calories".equals(field) ? "-0.1" : "10";
        String proteins = "proteins".equals(field) ? "-0.1" : "1";
        String fats = "fats".equals(field) ? "-0.1" : "1";
        String carbs = "carbs".equals(field) ? "-0.1" : "1";

        createDishWithNutrition("Блюдо отрицательное " + field, composition(productId, "100"), "100", "Второе", calories, proteins, fats, carbs)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors." + field + "[0]").value("Значение не может быть отрицательным"));
    }

    /**
     * Сумма ручных БЖУ может быть равна размеру порции,
     * но значение сразу выше порции должно отклоняться
     */
    @ParameterizedTest(name = "proteins={0}, fats={1}, carbs={2}, status={3}")
    @CsvSource({
            "30, 30, 40, 201",
            "30, 30, 40.1, 400"
    })
    @DisplayName("Проверяет верхнюю границу суммы БЖУ относительно порции")
    void shouldValidateManualMacrosAgainstPortionBoundary(String proteins, String fats, String carbs, int expectedStatus) throws Exception {
        String productId = createdProductId("Ингредиент границы порции", "10", "1", "1", "1", "Овощи", "Готовый к употреблению");

        createDishWithNutrition("Блюдо граница порции", composition(productId, "100"), "100", "Второе", "100", proteins, fats, carbs)
                .andExpect(status().is(expectedStatus));
    }

    /**
     * Каждый ручной макронутриент не может
     * превышать размер порции блюда
     */
    @ParameterizedTest(name = "{0}={1}, status={2}")
    @CsvSource({
            "proteins, 100, 201",
            "proteins, 100.1, 400",
            "fats, 100, 201",
            "fats, 100.1, 400",
            "carbs, 100, 201",
            "carbs, 100.1, 400"
    })
    @DisplayName("Проверяет верхнюю границу каждого БЖУ относительно порции")
    void shouldValidateManualMacroAgainstPortionBoundary(String macro, String value, int expectedStatus) throws Exception {
        String productId = createdProductId("Ингредиент БЖУ порции", "10", "1", "1", "1", "Овощи", "Готовый к употреблению");
        String proteins = "proteins".equals(macro) ? value : "0";
        String fats = "fats".equals(macro) ? value : "0";
        String carbs = "carbs".equals(macro) ? value : "0";

        createDishWithNutrition("Блюдо БЖУ порции", composition(productId, "100"), "100", "Второе", "100", proteins, fats, carbs)
                .andExpect(status().is(expectedStatus));
    }
}

// ---------------
// CRUD блюд
// --------------------

@DisplayName("CRUD API блюд")
class DishCrudApiIntegrationTest extends BaseApiIntegrationTest {
    /**
     * Блюдо с валидной категорией и существующим ингредиентом
     * создаётся и возвращается в ответе API
     */
    @ParameterizedTest(name = "category={0}")
    @CsvSource({
            "Десерт",
            "Первое",
            "Второе",
            "Напиток",
            "Салат",
            "Суп",
            "Перекус"
    })
    @DisplayName("Создаёт блюда допустимых категорий")
    void shouldCreateDishesForEveryCategory(String category) throws Exception {
        String productId = createdProductId("Ингредиент категории блюда", "20", "2", "2", "2", "Овощи", "Готовый к употреблению");
        String name = "Блюдо категории " + category;

        createDish(name, composition(productId, "100"), "500", category)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.category").value(category));
    }

    /**
     * Категория может быть задана макросом в названии,
     * после сохранения макрос удаляется из имени блюда
     */
    @ParameterizedTest(name = "macro={0}")
    @CsvSource({
            "!десерт, Десерт",
            "!первое, Первое",
            "!второе, Второе",
            "!напиток, Напиток",
            "!салат, Салат",
            "!суп, Суп",
            "!перекус, Перекус"
    })
    @DisplayName("Определяет категорию блюда по макросу в названии")
    void shouldParseDishCategoryMacro(String macro, String expectedCategory) throws Exception {
        String productId = createdProductId("Ингредиент макроса", "20", "2", "2", "2", "Овощи", "Готовый к употреблению");
        String baseName = "Блюдо с макросом";

        createDish(macro + " " + baseName, composition(productId, "100"), "500", "")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(baseName))
                .andExpect(jsonPath("$.category").value(expectedCategory));
    }

    /**
     * Можно заменить название, категорию, размер порции
     * и состав блюда
     */
    @ParameterizedTest(name = "update dish case={0}")
    @CsvSource({
            "1, Салат, 300"
    })
    @DisplayName("Обновляет блюда через API")
    void shouldUpdateDishes(String ignoredCase, String category, String portionSize) throws Exception {
        String firstProductId = createdProductId("Ингредиент до обновления", "20", "2", "2", "2", "Овощи", "Готовый к употреблению");
        String secondProductId = createdProductId("Ингредиент после обновления", "30", "3", "3", "3", "Крупы", "Полуфабрикат");
        String dishId = createdDishId("Блюдо до обновления", composition(firstProductId, "100"), "500", "Второе");
        String updatedName = "Блюдо после обновления";

        updateDish(dishId, updatedName, composition(secondProductId, "100"), portionSize, category)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dishId))
                .andExpect(jsonPath("$.name").value(updatedName))
                .andExpect(jsonPath("$.category").value(category))
                .andExpect(jsonPath("$.portionSize").value(Double.parseDouble(portionSize)));
    }

    /**
     * Блюдо удаляется, после чего чтение по id возвращает ошибку
     */
    @ParameterizedTest(name = "delete dish case={0}")
    @CsvSource({
            "1"
    })
    @DisplayName("Удаляет блюда")
    void shouldDeleteDishes(String index) throws Exception {
        String productId = createdProductId("Ингредиент удаляемого блюда " + index, "20", "2", "2", "2", "Овощи", "Готовый к употреблению");
        String dishId = createdDishId("Удаляемое блюдо " + index, composition(productId, "100"), "500", "Второе");

        mockMvc.perform(delete("/api/dishes/{id}", dishId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/dishes/{id}", dishId))
                .andExpect(status().isBadRequest());
    }

    /**
     * Некорректный размер порции отклоняется,
     * поэтому невалидное блюдо не попадает в базу
     */
    @ParameterizedTest(name = "invalid dish case={0}")
    @CsvSource({
            "1, BadPortion, 0, Второе, 400"
    })
    @DisplayName("Отклоняет невалидные блюда")
    void shouldRejectInvalidDishes(String ignoredCase, String rawName, String portionSize, String category, int expectedStatus) throws Exception {
        String productId = createdProductId("Ингредиент невалидного блюда", "100", "40", "40", "10", "Овощи", "Готовый к употреблению");
        String name = "A".equals(rawName) || "ZeroName".equals(rawName) ? "A" : rawName;

        createDish(name, composition(productId, "100"), portionSize, category)
                .andExpect(status().is(expectedStatus));
    }

    /**
     * Созданное блюдо должно читаться по id
     */
    @Test
    @DisplayName("Читает блюдо по id")
    void shouldReadDishById() throws Exception {
        String productId = createdProductId("Ингредиент чтения блюда", "20", "2", "2", "2", "Овощи", "Готовый к употреблению");
        String name = "Блюдо для чтения";
        String dishId = createdDishId(name, composition(productId, "100"), "500", "Второе");

        mockMvc.perform(get("/api/dishes/{id}", dishId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dishId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.compositionDetailed[0].product.id").value(productId));
    }

    /**
     * Несуществующий id блюда возвращает ошибку
     */
    @Test
    @DisplayName("Возвращает ошибку для несуществующего блюда")
    void shouldReturnBadRequestForMissingDish() throws Exception {
        mockMvc.perform(get("/api/dishes/{id}", "missing-dish-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Dish not found"));
    }

    /**
     * Несуществующий id блюда нельзя обновить
     */
    @Test
    @DisplayName("Возвращает ошибку при обновлении несуществующего блюда")
    void shouldReturnBadRequestWhenUpdatingMissingDish() throws Exception {
        String productId = createdProductId("Ингредиент missing update", "20", "2", "2", "2", "Овощи", "Готовый к употреблению");

        updateDish("missing-dish-id", "Нет блюда", composition(productId, "100"), "500", "Второе")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Dish not found"));
    }

    /**
     * Несуществующий id блюда нельзя удалить
     */
    @Test
    @DisplayName("Возвращает ошибку при удалении несуществующего блюда")
    void shouldReturnBadRequestWhenDeletingMissingDish() throws Exception {
        mockMvc.perform(delete("/api/dishes/{id}", "missing-dish-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Dish not found"));
    }

    /**
     * Фильтрация.  API должен учитывать категорию блюда и поисковую строку
     */
    @Test
    @DisplayName("Фильтрует блюда по категории и поиску")
    void shouldFilterDishesByCategoryAndSearch() throws Exception {
        String productId = createdProductId("Ингредиент фильтра блюд", "20", "2", "2", "2", "Овощи", "Готовый к употреблению");
        String token = "dish-filter";
        String soupName = "Суп " + token;
        createdDishId(soupName, composition(productId, "100"), "500", "Суп");
        createdDishId("Салат " + token, composition(productId, "100"), "500", "Салат");

        var result = mockMvc.perform(get("/api/dishes")
                        .param("category", "Суп")
                        .param("search", token))
                .andExpect(status().isOk())
                .andReturn();

        var dishes = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        org.assertj.core.api.Assertions.assertThat(textValues(dishes, "name"))
                .contains(soupName)
                .doesNotContain("Салат " + token);
    }

    /**
     * Сортировка. sort=portionSize возвращает блюда в порядке размера порции
     */
    @Test
    @DisplayName("Сортирует блюда по размеру порции")
    void shouldSortDishesByPortionSize() throws Exception {
        String productId = createdProductId("Ингредиент сортировки блюд", "20", "2", "2", "2", "Овощи", "Готовый к употреблению");
        String token = "portion-sort";
        String bigger = "Большое " + token;
        String smaller = "Малое " + token;
        createdDishId(bigger, composition(productId, "100"), "500", "Второе");
        createdDishId(smaller, composition(productId, "100"), "100", "Второе");

        var result = mockMvc.perform(get("/api/dishes")
                        .param("search", token)
                        .param("sort", "portionSize"))
                .andExpect(status().isOk())
                .andReturn();

        var dishes = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        org.assertj.core.api.Assertions.assertThat(indexOfText(dishes, "name", smaller)).isNotNegative();
        org.assertj.core.api.Assertions.assertThat(indexOfText(dishes, "name", bigger))
                .isGreaterThan(indexOfText(dishes, "name", smaller));
    }

    /**
     * ЭР Реизвестная категория блюда невалидна,
     * если её нельзя вывести из макроса
     */
    @Test
    @DisplayName("Отклоняет блюдо с некорректной категорией")
    void shouldRejectDishWithUnknownCategory() throws Exception {
        String productId = createdProductId("Ингредиент неизвестной категории", "20", "2", "2", "2", "Овощи", "Готовый к употреблению");

        createDish("Блюдо неизвестной категории", composition(productId, "100"), "500", "Завтрак")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors.category[0]").value("Категория обязательна"));
    }

    /**
     * У блюда нельзя сохранить больше пяти фото
     */
    @Test
    @DisplayName("Отклоняет блюдо с количеством фото больше лимита")
    void shouldRejectDishWithTooManyPhotos() throws Exception {
        String productId = createdProductId("Ингредиент фото блюда", "20", "2", "2", "2", "Овощи", "Готовый к употреблению");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/dishes")
                        .param("name", "Фото блюдо")
                        .param("photos", "[\"1.png\",\"2.png\",\"3.png\",\"4.png\",\"5.png\",\"6.png\"]")
                        .param("composition", composition(productId, "100"))
                        .param("portionSize", "500")
                        .param("category", "Второе")
                        .param("flags", flags(true, true, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors.photos[0]").value("Можно добавить не более 5 фото"));
    }
}

// --------------
// Связи продуктов и блюд
// ---------------

@DisplayName("API связи продуктов и блюд")
class ProductDishRelationApiIntegrationTest extends BaseApiIntegrationTest {
    /**
     * Детали блюда раскрывают связанные продукты
     * из состава через API
     */
    @ParameterizedTest(name = "details relation case={0}")
    @CsvSource({
            "1"
    })
    @DisplayName("Раскрывает продукты в составе блюда")
    void shouldExposeProductsInsideDishComposition(String index) throws Exception {
        String productName = "Связанный продукт " + index;
        String productId = createdProductId(productName, "30", "3", "3", "3", "Овощи", "Готовый к употреблению");
        String dishId = createdDishId("Блюдо со связью " + index, composition(productId, "100"), "500", "Второе");

        mockMvc.perform(get("/api/dishes/{id}", dishId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compositionDetailed[0].product.id").value(productId))
                .andExpect(jsonPath("$.compositionDetailed[0].product.name").value(productName));
    }

    /**
     * Продукт нельзя удалить,
     * пока он используется хотя бы в одном блюде
     */
    @ParameterizedTest(name = "delete conflict case={0}")
    @CsvSource({
            "1"
    })
    @DisplayName("Запрещает удалять продукт, используемый в блюде")
    void shouldPreventDeletingProductsUsedByDishes(String index) throws Exception {
        String productId = createdProductId("Занятый продукт " + index, "30", "3", "3", "3", "Овощи", "Готовый к употреблению");
        String dishName = "Блюдо блокирует продукт " + index;
        createdDishId(dishName, composition(productId, "100"), "500", "Второе");

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot delete product used in dishes"))
                .andExpect(jsonPath("$.dishes[0].name").value(dishName));
    }

    /**
     * После удаления блюда продукт больше
     * не имеет зависимостей и может быть удалён
     */
    @ParameterizedTest(name = "delete after dish case={0}")
    @CsvSource({
            "1"
    })
    @DisplayName("Разрешает удалить продукт после удаления блюда")
    void shouldAllowDeletingProductAfterDishDeletion(String index) throws Exception {
        String productId = createdProductId("Освобождаемый продукт " + index, "30", "3", "3", "3", "Овощи", "Готовый к употреблению");
        String dishId = createdDishId("Удаляемое связанное блюдо " + index, composition(productId, "100"), "500", "Второе");

        mockMvc.perform(delete("/api/dishes/{id}", dishId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNoContent());
    }

    /**
     * Доступность флагов блюда вычисляется
     * как пересечение флагов всех продуктов состава
     */
    @ParameterizedTest(name = "flags relation case={0}")
    @CsvSource({
            "5, false, false, false"
    })
    @DisplayName("Рассчитывает доступные флаги блюда по продуктам")
    void shouldResolveDishFlagsFromProducts(String index, boolean vegan, boolean glutenFree, boolean sugarFree) throws Exception {
        String productId = createdProductIdWithFlags(
                "Продукт флагов " + index,
                "30",
                "3",
                "3",
                "3",
                "Овощи",
                "Готовый к употреблению",
                flags(vegan, glutenFree, sugarFree)
        );

        createDish("Блюдо флагов " + index, composition(productId, "100"), "500", "Второе")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableFlags.vegan").value(vegan))
                .andExpect(jsonPath("$.availableFlags.glutenFree").value(glutenFree))
                .andExpect(jsonPath("$.availableFlags.sugarFree").value(sugarFree))
                .andExpect(jsonPath("$.flags.vegan").value(vegan))
                .andExpect(jsonPath("$.flags.glutenFree").value(glutenFree))
                .andExpect(jsonPath("$.flags.sugarFree").value(sugarFree));
    }


    /**
     * Если продукт используется в нескольких блюдах,
     * API должен вернуть все блокирующие зависимости
     */
    @Test
    @DisplayName("Возвращает все блюда, которые блокируют удаление продукта")
    void shouldReturnAllDishesBlockingProductDeletion() throws Exception {
        String productId = createdProductId("Общий продукт", "30", "3", "3", "3", "Овощи", "Готовый к употреблению");
        String firstDish = "Первое блокирующее блюдо";
        String secondDish = "Второе блокирующее блюдо";
        createdDishId(firstDish, composition(productId, "100"), "500", "Второе");
        createdDishId(secondDish, composition(productId, "50"), "300", "Суп");

        var result = mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot delete product used in dishes"))
                .andReturn();

        var dishes = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("dishes");
        org.assertj.core.api.Assertions.assertThat(dishes).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(dishes.findValuesAsText("name")).contains(firstDish, secondDish);
    }

    /**
     * Флаг блюда становится false,
     * если хотя бы один продукт состава не поддерживает этот флаг
     */
    @Test
    @DisplayName("Вычисляет пересечение флагов нескольких продуктов")
    void shouldResolveFlagsAsIntersectionOfIngredientFlags() throws Exception {
        String veganProductId = createdProductIdWithFlags(
                "Веганский продукт",
                "20",
                "2",
                "2",
                "2",
                "Овощи",
                "Готовый к употреблению",
                flags(true, true, true)
        );
        String nonVeganProductId = createdProductIdWithFlags(
                "Не веганский продукт",
                "40",
                "4",
                "4",
                "4",
                "Мясной",
                "Требует приготовления",
                flags(false, true, false)
        );

        createDish("Блюдо пересечение флагов", composition(veganProductId, "100", nonVeganProductId, "100"), "500", "Второе")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableFlags.vegan").value(false))
                .andExpect(jsonPath("$.availableFlags.glutenFree").value(true))
                .andExpect(jsonPath("$.availableFlags.sugarFree").value(false))
                .andExpect(jsonPath("$.flags.vegan").value(false))
                .andExpect(jsonPath("$.flags.glutenFree").value(true))
                .andExpect(jsonPath("$.flags.sugarFree").value(false));
    }

    /**
     * Список блюд должен фильтроваться по рассчитанным флагам
     */
    @Test
    @DisplayName("Фильтрует блюда по доступным флагам")
    void shouldFilterDishesByResolvedFlags() throws Exception {
        String token = "dish-flag";
        String veganProductId = createdProductIdWithFlags(
                "Флаговый овощ",
                "20",
                "2",
                "2",
                "2",
                "Овощи",
                "Готовый к употреблению",
                flags(true, true, true)
        );
        String meatProductId = createdProductIdWithFlags(
                "Флаговое мясо",
                "40",
                "4",
                "4",
                "4",
                "Мясной",
                "Требует приготовления",
                flags(false, true, true)
        );
        String veganDish = "Веганское " + token;
        createdDishId(veganDish, composition(veganProductId, "100"), "500", "Второе");
        createdDishId("Мясное " + token, composition(meatProductId, "100"), "500", "Второе");

        var result = mockMvc.perform(get("/api/dishes")
                        .param("search", token)
                        .param("vegan", "true"))
                .andExpect(status().isOk())
                .andReturn();

        var dishes = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        org.assertj.core.api.Assertions.assertThat(textValues(dishes, "name"))
                .contains(veganDish)
                .doesNotContain("Мясное " + token);
    }
}

package com.example.recipebook.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.example.recipebook.ui.pages.DishesPage;
import com.example.recipebook.ui.pages.ProductsPage;
import com.example.recipebook.ui.pages.ProductsPage.ProductForm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ResourceLock("recipe-book-database")
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Системные UI-тесты книги рецептов")
class UiRecipeBookTest {
    private static Playwright playwright;
    private static Browser browser;
    @LocalServerPort
    private int port;

    private BrowserContext context;
    private Page page;
    private ProductsPage productsPage;
    private DishesPage dishesPage;

    @BeforeAll
    static void startBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void stopBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void createCleanBrowserContext() {
        context = browser.newContext();
        page = context.newPage();
        page.setDefaultTimeout(15000);
        productsPage = new ProductsPage(page, baseUrl());
        dishesPage = new DishesPage(page, baseUrl());
    }

    @AfterEach
    void closeBrowserContext() {
        if (context != null) context.close();
    }

    @ParameterizedTest(name = "calories={0}, saved={1}")
    @CsvSource({
            "-0.1, false",
            "0, true",
            "0.1, true"
    })
    @DisplayName("Проверяет нижнюю границу калорийности продукта через интерфейс")
    void shouldValidateProductCaloriesLowerBoundaryThroughUi(String calories, boolean saved) {
        productsPage.open();
        String name = uniqueName("Интерфейс Калорийность");

        productsPage.fillForm(productForm(name, calories, "0", "0", "0", "Овощи", "Готовый к употреблению", true));

        if (saved) {
            productsPage.save().shouldContainProduct(name);
        } else {
            String alert = productsPage.saveExpectingAlert();
            org.assertj.core.api.Assertions.assertThat(alert)
                    .contains("calories")
                    .contains("Значение не может быть отрицательным");
        }
    }

    @ParameterizedTest(name = "nameLengthCase={0}, saved={1}")
    @CsvSource({
            "A, false",
            "Ab, true",
            "Aba, true"
    })
    @DisplayName("Проверяет нижнюю границу длины названия продукта через интерфейс")
    void shouldValidateProductNameLowerBoundaryThroughUi(String namePrefix, boolean saved) {
        productsPage.open();
        String name = namePrefix;

        productsPage.fillForm(productForm(name, "10", "1", "1", "1", "Овощи", "Готовый к употреблению", true));

        if (saved) {
            productsPage.save().shouldContainProduct(name);
        } else {
            String alert = productsPage.saveExpectingAlert();
            org.assertj.core.api.Assertions.assertThat(alert)
                    .contains("name")
                    .contains("Минимальная длина названия");
        }
    }

    @Test
    @DisplayName("Фильтрует продукты по признаку веганского продукта через интерфейс")
    void shouldFilterProductsByVeganFlagThroughUi() {
        productsPage.open();
        String token = runToken();
        String veganName = "Интерфейс Веганский " + token;
        String meatName = "Интерфейс Мясной " + token;

        productsPage.fillForm(productForm(veganName, "45", "3", "2", "4", "Овощи", "Готовый к употреблению", true)).save();
        productsPage.fillForm(productForm(meatName, "80", "10", "5", "0", "Мясной", "Требует приготовления", false)).save();

        productsPage.search(token)
                .filterVegan()
                .shouldNotContainProductInCurrentList(meatName);
        assertThat(productsPage.productCard(veganName)).isVisible();
    }

    @Test
    @DisplayName("Создаёт блюдо и рассчитывает КБЖУ через интерфейс")
    void shouldCreateDishAndCalculateNutritionThroughUi() {
        String productName = createUiProduct("Интерфейс Картофель", "77", "2", "0.4", "16.3", "Овощи", "Требует приготовления", true, true, true);

        dishesPage.open();
        String dishName = uniqueName("Интерфейс Блюдо");
        dishesPage.fillBaseDishFields(dishName, productName, "100", "250", "Второе")
                .shouldShowCalculatedNutrition("77", "2", "0.4", "16.3")
                .save()
                .shouldContainDish(dishName);
        assertThat(dishesPage.dishCard(dishName)).containsText("КБЖУ: 77.00 / 2.00 / 0.40 / 16.30");
    }

    @ParameterizedTest(name = "amount={0}, accepted={1}")
    @CsvSource({
            "-0.01, false",
            "0, false",
            "0.01, true"
    })
    @DisplayName("Проверяет нижнюю границу количества ингредиента через интерфейс")
    void shouldValidateIngredientAmountLowerBoundaryThroughUi(String amount, boolean accepted) {
        String productName = createUiProduct("Интерфейс Ингредиент", "20", "2", "2", "2", "Овощи", "Готовый к употреблению", true, true, true);

        dishesPage.open();

        if (accepted) {
            dishesPage.addIngredient(productName, amount)
                    .shouldShowIngredientInComposition(productName, "0.01");
        } else {
            String alert = dishesPage.addIngredientExpectingAlert(productName, amount);
            org.assertj.core.api.Assertions.assertThat(alert).contains("Укажите количество продукта больше 0");
        }
    }

    @ParameterizedTest(name = "{0}={1}, saved={2}")
    @CsvSource({
            "proteins, 100, true",
            "proteins, 100.1, false",
            "fats, 100, true",
            "fats, 100.1, false",
            "carbs, 100, true",
            "carbs, 100.1, false"
    })
    @DisplayName("Проверяет верхнюю границу БЖУ продукта через интерфейс")
    void shouldValidateProductMacroUpperBoundaryThroughUi(String macro, String value, boolean saved) {
        productsPage.open();
        String name = uniqueName("Интерфейс Продукт БЖУ");
        String proteins = "proteins".equals(macro) ? value : "0";
        String fats = "fats".equals(macro) ? value : "0";
        String carbs = "carbs".equals(macro) ? value : "0";

        productsPage.fillForm(productForm(name, "100", proteins, fats, carbs, "Овощи", "Готовый к употреблению", true));

        if (saved) {
            productsPage.save().shouldContainProduct(name);
        } else {
            String alert = productsPage.saveExpectingAlert();
            org.assertj.core.api.Assertions.assertThat(alert)
                    .contains(macro)
                    .contains("Значение не может превышать 100");
        }
    }

    @ParameterizedTest(name = "portionSize={0}, saved={1}")
    @CsvSource({
            "-0.1, false",
            "0, false",
            "0.1, true"
    })
    @DisplayName("Проверяет нижнюю границу размера порции блюда через интерфейс")
    void shouldValidateDishPortionLowerBoundaryThroughUi(String portionSize, boolean saved) {
        String productName = createUiProduct("Интерфейс Ингредиент порции", "0", "0", "0", "0", "Жидкость", "Готовый к употреблению", true, true, true);
        dishesPage.open();
        String dishName = uniqueName("Интерфейс Блюдо с порцией");

        dishesPage.fillBaseDishFields(dishName, productName, "0.1", portionSize, "Напиток");

        if (saved) {
            dishesPage.save().shouldContainDish(dishName);
        } else {
            String alert = dishesPage.saveExpectingAlert();
            org.assertj.core.api.Assertions.assertThat(alert)
                    .contains("portionSize")
                    .contains("Размер порции должен быть больше 0");
        }
    }

    @ParameterizedTest(name = "proteins={0}, saved={1}")
    @CsvSource({
            "100, true",
            "100.1, false"
    })
    @DisplayName("Проверяет верхнюю границу ручных белков относительно порции через интерфейс")
    void shouldValidateManualDishProteinUpperBoundaryThroughUi(String proteins, boolean saved) {
        String productName = createUiProduct("Интерфейс Ручное БЖУ", "10", "1", "1", "1", "Овощи", "Готовый к употреблению", true, true, true);
        dishesPage.open();
        String dishName = uniqueName("Интерфейс Блюдо БЖУ");

        dishesPage.fillBaseDishFields(dishName, productName, "100", "100", "Второе")
                .fillManualNutrition("100", proteins, "0", "0");

        if (saved) {
            dishesPage.save().shouldContainDish(dishName);
        } else {
            String alert = dishesPage.saveExpectingAlert();
            org.assertj.core.api.Assertions.assertThat(alert)
                    .contains("proteins")
                    .contains("Значение не может превышать размер порции блюда");
        }
    }

    @Test
    @DisplayName("Отображает каталог продуктов без горизонтального скролла на мобильном viewport")
    void shouldAdaptProductsCatalogToMobileViewport() {
        page.setViewportSize(390, 844);

        productsPage.open();

        assertThat(page.locator(".topbar")).isVisible();
        assertThat(page.locator(".nav-links")).isVisible();
        assertThat(page.locator("#p-name")).isVisible();
        assertThat(page.locator("#p-search")).isVisible();
        assertThat(page.locator("#p-refresh")).isVisible();
        org.assertj.core.api.Assertions.assertThat(hasNoHorizontalOverflow()).isTrue();
    }

    @Test
    @DisplayName("Перестраивает форму и каталог блюд в одну колонку на планшетном viewport")
    void shouldAdaptDishesPageToTabletViewport() {
        page.setViewportSize(768, 1024);

        dishesPage.open();

        var createPanelBox = page.locator("#create-dish").boundingBox();
        var catalogPanelBox = page.locator("main.page-grid > section").nth(1).boundingBox();
        org.assertj.core.api.Assertions.assertThat(catalogPanelBox.y)
                .isGreaterThan(createPanelBox.y + createPanelBox.height);
        assertThat(page.locator("#d-product-select")).isVisible();
        assertThat(page.locator("#d-save")).isVisible();
        assertThat(page.locator("#d-search")).isVisible();
        org.assertj.core.api.Assertions.assertThat(hasNoHorizontalOverflow()).isTrue();
    }

    private boolean hasNoHorizontalOverflow() {
        return (boolean) page.evaluate(
                "() => document.documentElement.scrollWidth <= document.documentElement.clientWidth"
        );
    }

    private String createUiProduct(String prefix, String calories, String proteins, String fats, String carbs,
                                   String category, String cookingNeed,
                                   boolean vegan, boolean glutenFree, boolean sugarFree) {
        productsPage.open();
        String name = uniqueName(prefix);
        productsPage.fillForm(new ProductForm(name, calories, proteins, fats, carbs, category, cookingNeed, vegan, glutenFree, sugarFree))
                .save()
                .shouldContainProduct(name);
        return name;
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private ProductForm productForm(String name, String calories, String proteins, String fats, String carbs,
                                    String category, String cookingNeed, boolean vegan) {
        return new ProductForm(name, calories, proteins, fats, carbs, category, cookingNeed, vegan, true, true);
    }

    private String uniqueName(String prefix) {
        return prefix + " " + runToken();
    }

    private String runToken() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}

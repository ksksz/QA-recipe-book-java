package com.example.recipebook.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class DishesPage extends BasePage {
    public DishesPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public DishesPage open() {
        open("/dishes.html");
        assertVisible("#dishes");
        assertThat(page.locator("#d-product-select")).isEnabled();
        return this;
    }

    public DishesPage fillName(String name) {
        page.locator("#d-name").fill(name);
        return this;
    }

    public DishesPage addIngredient(String productName, String amount) {
        selectByLabel("#d-product-select", productName);
        page.locator("#d-product-amount").fill(amount);
        page.locator("#d-add-product").click();
        return this;
    }

    public String addIngredientExpectingAlert(String productName, String amount) {
        selectByLabel("#d-product-select", productName);
        page.locator("#d-product-amount").fill(amount);
        return acceptAlertAfter(() -> page.locator("#d-add-product").click());
    }

    public DishesPage fillBaseDishFields(String name, String productName, String amount, String portionSize, String category) {
        fillName(name);
        addIngredient(productName, amount);
        page.locator("#d-portion").fill(portionSize);
        selectByLabel("#d-category", category);
        return this;
    }

    public DishesPage fillManualNutrition(String calories, String proteins, String fats, String carbs) {
        page.locator("#d-calories").fill(calories);
        page.locator("#d-proteins").fill(proteins);
        page.locator("#d-fats").fill(fats);
        page.locator("#d-carbs").fill(carbs);
        return this;
    }

    public DishesPage save() {
        page.locator("#d-save").click();
        return this;
    }

    public String saveExpectingAlert() {
        return acceptAlertAfter(() -> page.locator("#d-save").click());
    }

    public DishesPage search(String text) {
        page.locator("#d-search").fill(text);
        page.locator("#d-refresh").click();
        return this;
    }

    public DishesPage shouldContainDish(String name) {
        search(name);
        assertThat(dishCard(name)).isVisible();
        return this;
    }

    public DishesPage shouldShowCalculatedNutrition(String calories, String proteins, String fats, String carbs) {
        assertThat(page.locator("#d-calories")).hasValue(calories);
        assertThat(page.locator("#d-proteins")).hasValue(proteins);
        assertThat(page.locator("#d-fats")).hasValue(fats);
        assertThat(page.locator("#d-carbs")).hasValue(carbs);
        return this;
    }

    public DishesPage shouldShowIngredientInComposition(String productName, String amount) {
        assertThat(page.locator("#d-composition-list")).containsText(productName);
        assertThat(page.locator("[data-composition-amount]")).hasValue(amount);
        return this;
    }

    public Locator dishCard(String text) {
        return card("#dishes", text);
    }
}

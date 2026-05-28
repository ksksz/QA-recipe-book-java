package com.example.recipebook.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class ProductsPage extends BasePage {
    public ProductsPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public ProductsPage open() {
        open("/products.html");
        assertVisible("#products");
        return this;
    }

    public ProductsPage fillForm(ProductForm form) {
        page.locator("#p-name").fill(form.name());
        page.locator("#p-calories").fill(form.calories());
        page.locator("#p-proteins").fill(form.proteins());
        page.locator("#p-fats").fill(form.fats());
        page.locator("#p-carbs").fill(form.carbs());
        page.locator("#p-composition").fill("UI system test data");
        selectByLabel("#p-category", form.category());
        selectByLabel("#p-cooking", form.cookingNeed());
        setChecked("#p-vegan", form.vegan());
        setChecked("#p-glutenFree", form.glutenFree());
        setChecked("#p-sugarFree", form.sugarFree());
        return this;
    }

    public ProductsPage save() {
        waitForApiResponse("/api/products", "GET", () -> saveButton().click());
        return this;
    }

    public String saveExpectingAlert() {
        return acceptAlertAfter(() -> saveButton().click());
    }

    public ProductsPage search(String text) {
        page.locator("#p-search").fill(text);
        page.locator("#p-refresh").click();
        return this;
    }

    public ProductsPage filterVegan() {
        page.locator("#pf-vegan").check();
        page.locator("#p-refresh").click();
        return this;
    }

    public ProductsPage shouldContainProduct(String name) {
        search(name);
        assertThat(productCard(name)).isVisible();
        return this;
    }

    public ProductsPage shouldNotContainProductInCurrentList(String name) {
        assertThat(page.locator("#products")).not().containsText(name);
        return this;
    }

    public Locator productCard(String text) {
        return card("#products", text);
    }

    private Locator saveButton() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Сохранить продукт"));
    }

    public record ProductForm(
            String name,
            String calories,
            String proteins,
            String fats,
            String carbs,
            String category,
            String cookingNeed,
            boolean vegan,
            boolean glutenFree,
            boolean sugarFree
    ) {
    }
}

package com.example.recipebook.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import java.util.concurrent.atomic.AtomicReference;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public abstract class BasePage {
    protected final Page page;
    private final String baseUrl;

    protected BasePage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    protected void open(String path) {
        page.navigate(baseUrl + path);
    }

    protected void selectByLabel(String selector, String label) {
        page.locator(selector).selectOption(new SelectOption().setLabel(label));
    }

    protected void setChecked(String selector, boolean checked) {
        Locator checkbox = page.locator(selector);
        if (checked) checkbox.check();
        else checkbox.uncheck();
    }

    protected Locator card(String rootSelector, String text) {
        return page.locator(rootSelector + " .card")
                .filter(new Locator.FilterOptions().setHasText(text))
                .first();
    }

    protected void waitForApiResponse(String path, String method, Runnable action) {
        page.waitForResponse(
                response -> response.url().contains(path)
                        && method.equals(response.request().method())
                        && response.status() < 400,
                action
        );
    }

    public String acceptAlertAfter(Runnable action) {
        AtomicReference<String> message = new AtomicReference<>();
        page.onceDialog(dialog -> {
            message.set(dialog.message());
            dialog.accept();
        });
        action.run();
        page.waitForCondition(() -> message.get() != null);
        org.assertj.core.api.Assertions.assertThat(message.get())
                .as("Ожидался alert с ошибкой валидации")
                .isNotBlank();
        return message.get();
    }

    protected void assertVisible(String selector) {
        assertThat(page.locator(selector)).isVisible();
    }
}

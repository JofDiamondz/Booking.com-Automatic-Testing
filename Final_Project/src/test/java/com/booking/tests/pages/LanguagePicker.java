package com.booking.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LanguagePicker {

    private WebDriver driver;
    private WebDriverWait wait;

    public LanguagePicker(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    // Works for desktop + mobile layouts
    private By languageButton = By.cssSelector(
            "button[data-testid='header-language-picker-trigger'], " +
                    "button[aria-label='Select your language'], " +
                    "button[data-testid='header-mobile-menu-trigger']"
    );

    // Modal container
    private By modal = By.cssSelector("div[data-testid='header-mobile-menu-language-picker-modal']");

    // Close button inside modal
    private By closeButton = By.cssSelector("button[aria-label='Close language selector']");

    // ---------------------------
    // ACTIONS
    // ---------------------------

    public void openLanguagePicker() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(languageButton));
        btn.click();
    }

    public boolean isModalVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(modal));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Select ANY language dynamically
    public void selectLanguage(String languageName) {
        By languageOption = By.xpath(
                "//button[@data-testid='selection-item']//span[contains(text(),'" + languageName + "')]"
        );

        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(languageOption));
        option.click();
    }

    // Select multiple languages in sequence
    public void selectMultipleLanguages(String... languages) {
        for (String lang : languages) {
            openLanguagePicker();
            selectLanguage(lang);

            // Wait for URL to update after each selection
            try { Thread.sleep(1500); } catch (Exception ignored) {}
        }
    }

    public void closeLanguagePicker() {
        WebElement close = wait.until(ExpectedConditions.elementToBeClickable(closeButton));
        close.click();
    }
}

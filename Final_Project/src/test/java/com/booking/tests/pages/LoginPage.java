package com.booking.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {

    private WebDriver driver;
    private WebDriverWait wait;

    private By headerRoot = By.cssSelector("header.Header_root");
    private By signInButton = By.cssSelector("a[data-testid='header-small-sign-in-button']");
    private By emailField = By.id("username");
    private By continueBtn = By.xpath("//span[contains(text(),'Continue with email')]");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void openLogin() {

        // ⭐ Wait for header to finish re-rendering after popup closes
        wait.until(ExpectedConditions.presenceOfElementLocated(headerRoot));

        // ⭐ Wait for the sign-in button to appear in the DOM
        WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(signInButton));

        // ⭐ Wait for it to be clickable
        wait.until(ExpectedConditions.elementToBeClickable(signIn)).click();

        // ⭐ Confirm navigation
        wait.until(ExpectedConditions.urlContains("account"));
    }

    public void enterEmail(String email) {
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        emailInput.clear();
        emailInput.sendKeys(email);
    }

    public void clickContinue() {
        wait.until(ExpectedConditions.elementToBeClickable(continueBtn)).click();
    }
}

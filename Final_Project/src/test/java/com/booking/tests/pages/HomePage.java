package com.booking.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomePage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By destinationInput = By.name("ss");
    private By searchButton = By.cssSelector("button[type='submit']");

    public HomePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void enterDestination(String destination) {

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(destinationInput));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);

        input.clear();
        input.sendKeys(destination);

        try {
            WebElement firstOption = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector("li[data-testid='autocomplete-result']")
                    )
            );
            firstOption.click();
        } catch (Exception ignored) {}
    }

    public void clickSearch() {
        wait.until(ExpectedConditions.elementToBeClickable(searchButton)).click();
    }

    public void searchForDestination(String destination) {
        enterDestination(destination);
        clickSearch();
    }
}

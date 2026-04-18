package com.booking.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CommonPopupsHandler {
    private WebDriver driver;
    private WebDriverWait wait;

    public CommonPopupsHandler(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    // Booking.com cookie popup (most reliable locator)
    public void handleCookiesIfPresent() {
        try {
            WebElement cookieBtn = driver.findElement(By.id("onetrust-accept-btn-handler"));
            if (cookieBtn.isDisplayed()) {
                cookieBtn.click();
                System.out.println("Cookie popup closed.");
            }
        } catch (Exception ignored) {}
    }

    // Booking.com sign-in popup
    public void handleSignInPopupIfPresent() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement closeBtn = shortWait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[@aria-label='Dismiss sign-in info.']")
                    )
            );
            closeBtn.click();
            System.out.println("Sign-in popup closed.");
        } catch (Exception ignored) {}
    }

    // Newsletter / generic modal
    public void handleNewsletterOrGenericModalIfPresent() {
        List<By> closeButtons = List.of(
                By.cssSelector("button[aria-label*='Dismiss']"),
                By.cssSelector("button[aria-label*='Close']"),
                By.cssSelector("button[aria-label='Close']"),
                By.cssSelector("button[aria-label='Dismiss']"),
                By.cssSelector("button[aria-label='Not now']"),
                By.cssSelector("button[aria-label='No thanks']")
        );

        for (By locator : closeButtons) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    elements.get(0).click();
                    System.out.println("Generic popup closed.");
                    break;
                }
            } catch (Exception ignored) {}
        }
    }

    // Call this once after page load
    public void handleAllPopups() {
        handleCookiesIfPresent();
        handleSignInPopupIfPresent();
        handleNewsletterOrGenericModalIfPresent();
    }
}

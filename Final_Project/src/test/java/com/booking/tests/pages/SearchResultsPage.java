package com.booking.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class SearchResultsPage {
    private WebDriver driver;

    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
    }

    public void waitForResultsPageToLoad() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='property-card']")
        ));

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 800);");
    }

    public void waitForFiltersToLoad() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));

        WebElement sidebar = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[data-testid='filters-sidebar']")
        ));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", sidebar);
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 600);");
    }

    public int getResultCount() {
        List<WebElement> cards = driver.findElements(By.cssSelector("[data-testid='property-card']"));
        return cards.size();
    }

    public void waitForResultsToRefresh() {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));

        try {
            shortWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[data-testid='property-card']")
            ));
        } catch (Exception ignored) {}
    }

    // ⭐ ALWAYS WORKS — clicks if available, skips if not
    public boolean clickFreeWifiFilter() {
        try {
            List<WebElement> wifiFilters = driver.findElements(
                    By.cssSelector("input[name='hotelfacility=107']")
            );

            if (wifiFilters.isEmpty()) {
                System.out.println("Free WiFi filter NOT available on this run.");
                return false;
            }

            WebElement wifi = wifiFilters.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", wifi);
            wifi.click();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    // ⭐ YOU ASKED FOR THIS — EMPTY RESULTS DETECTOR
    public boolean hasEmptyResultsOrErrorMessage() {

        if (!driver.findElements(By.cssSelector("[data-testid='no-results-header']")).isEmpty()) {
            return true;
        }

        if (!driver.findElements(By.cssSelector("[data-testid='no-results-message']")).isEmpty()) {
            return true;
        }

        if (!driver.findElements(By.xpath("//*[contains(text(),'Did you mean')]")).isEmpty()) {
            return true;
        }

        if (!driver.findElements(By.xpath("//*[contains(text(),'couldn')]")).isEmpty()) {
            return true;
        }

        if (getResultCount() == 0) {
            return true;
        }

        return false;
    }

    public int getStarRatingFilterCount() {
        List<WebElement> stars = driver.findElements(By.xpath(
                "//*[contains(text(),'stars') or contains(text(),'Star rating')]"
        ));
        return stars.size();
    }
    public void openFirstProperty() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));

        // Wait for property cards to load
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='property-card']")
        ));

        // Click the first property title link
        WebElement firstProperty = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='property-card'] a[data-testid='title-link']")
        ));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstProperty);
        firstProperty.click();
    }

    public boolean isSortDropdownPresent() {
        List<WebElement> sorts = driver.findElements(By.xpath(
                "//button[contains(@data-testid,'sorters-dropdown-trigger')] | " +
                        "//select | " +
                        "//*[contains(text(),'Sort by')]"
        ));
        return !sorts.isEmpty();
    }

    public boolean isMapToggleVisible() {
        List<WebElement> maps = driver.findElements(By.xpath(
                "//*[contains(text(),'Show on map')] | //*[@data-testid='map-trigger']"
        ));
        return !maps.isEmpty();
    }
}

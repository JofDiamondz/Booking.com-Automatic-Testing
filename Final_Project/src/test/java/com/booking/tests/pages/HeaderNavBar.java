package com.booking.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HeaderNavBar {

    private WebDriver driver;
    private WebDriverWait wait;

    public HeaderNavBar(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    private By staysTab = By.id("accommodations");
    private By flightsTab = By.id("flights");
    private By packagesTab = By.id("packages");
    private By carsTab = By.id("cars");
    private By attractionsTab = By.id("attractions");
    private By taxisTab = By.id("airport_taxis");

    public void clickStays() {
        wait.until(ExpectedConditions.elementToBeClickable(staysTab)).click();
    }

    public void clickFlights() {
        wait.until(ExpectedConditions.elementToBeClickable(flightsTab)).click();
    }

    public void clickPackages() {
        wait.until(ExpectedConditions.elementToBeClickable(packagesTab)).click();
    }

    public void clickCars() {
        wait.until(ExpectedConditions.elementToBeClickable(carsTab)).click();
    }

    public void clickAttractions() {
        wait.until(ExpectedConditions.elementToBeClickable(attractionsTab)).click();
    }

    public void clickAirportTaxis() {
        wait.until(ExpectedConditions.elementToBeClickable(taxisTab)).click();
    }
}

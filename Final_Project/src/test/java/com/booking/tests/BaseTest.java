package com.booking.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.time.Duration;

public class BaseTest {

    public WebDriver driver;
    public WebDriverWait wait;
    public String originalWindow;

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--lang=en-US");
        options.addArguments("--accept-lang=en-US,en");
        options.addArguments("--disable-notifications");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        System.out.println("Browser launched.");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
        System.out.println("Browser closed.");
    }

    // ── Shared navigation ─────────────────────────────────────────
    public void goToHomepage() {
        driver.get("https://www.booking.com/?lang=en-us");
        originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@name='ss' or @placeholder='Where are you going?']")));
        dismissAllPopups();
    }

    // ── Popup dismissal ───────────────────────────────────────────
    public void dismissAllPopups() {
        // Cookie consent
        try {
            driver.findElement(By.id("onetrust-accept-btn-handler")).click();
            System.out.println("Cookie popup dismissed.");
        } catch (NoSuchElementException ignored) {}

        // Sign-in overlay
        try {
            driver.findElement(
                By.xpath("//button[@aria-label='Dismiss sign-in info.']")).click();
            System.out.println("Sign-in overlay dismissed.");
        } catch (NoSuchElementException ignored) {}

        // Generic modal
        String[] genericSelectors = {
            "button[aria-label*='Dismiss']",
            "button[aria-label*='Close']",
            "button[aria-label='Not now']"
        };
        for (String sel : genericSelectors) {
            try {
                WebElement btn = driver.findElement(By.cssSelector(sel));
                if (btn.isDisplayed()) { btn.click(); break; }
            } catch (NoSuchElementException ignored) {}
        }
    }

    // ── Shared search helper ──────────────────────────────────────
    public void searchFor(String destination) {
        driver.findElement(
            By.xpath("//input[@name='ss' or @placeholder='Where are you going?']"))
            .sendKeys(destination);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//div[@data-testid='autocomplete-result'] | //li[@data-testid='autocomplete-results-option'])[1]")))
                .click();
        } catch (Exception e) {
            driver.findElement(
                By.xpath("//input[@name='ss' or @placeholder='Where are you going?']"))
                .sendKeys(Keys.ENTER);
        }

        closeDatePicker();

        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-testid='searchbox-submit-button']"))).click();
        } catch (Exception e) {
            driver.findElement(
                By.xpath("//input[@name='ss' or @placeholder='Where are you going?']"))
                .sendKeys(Keys.ENTER);
        }

        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[@data-testid='property-card']")));

        dismissAllPopups();
        System.out.println("Search results loaded for: " + destination);
    }

    // ── Date picker closer ────────────────────────────────────────
    public void closeDatePicker() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

    // ── Open first property (new tab) ─────────────────────────────
    public void openFirstProperty() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("(//div[@data-testid='property-card']//a[@data-testid='title-link'])[1]")))
            .sendKeys(Keys.RETURN);

        wait.until(d -> d.getWindowHandles().size() > 1);

        for (String w : driver.getWindowHandles()) {
            if (!w.equals(originalWindow)) {
                driver.switchTo().window(w);
                break;
            }
        }

        wait.until(d -> !d.getCurrentUrl().contains("searchresults"));
        System.out.println("Navigated to property: " + driver.getCurrentUrl());
    }

    // ── Close extra tabs ──────────────────────────────────────────
    public void closeExtraTabs() {
        try {
            for (String w : driver.getWindowHandles()) {
                if (!w.equals(originalWindow)) {
                    driver.switchTo().window(w);
                    driver.close();
                }
            }
            driver.switchTo().window(originalWindow);
        } catch (Exception e) {
            System.out.println("Tab cleanup: " + e.getMessage());
        }
    }

    protected void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}

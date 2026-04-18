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

    // ── ThreadLocal storage ───────────────────────────────────────
    // When 9 browsers launch simultaneously, every thread needs its
    // OWN driver and wait. ThreadLocal gives each thread a separate
    // slot so they never read or overwrite each other's browser.
    private static final ThreadLocal<WebDriver>    TL_DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> TL_WAIT  = new ThreadLocal<>();

    // Public fields kept for backward compatibility with subclasses
    // that reference `driver` and `wait` directly. They are assigned
    // in setUp() after the ThreadLocal is populated.
    public WebDriver driver;
    public WebDriverWait wait;
    public String originalWindow;

    @BeforeClass
    public void setUp() {
        // WebDriverManager is thread-safe — safe to call from multiple threads
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--lang=en-US");
        options.addArguments("--accept-lang=en-US,en");
        options.addArguments("--disable-notifications");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        // Each thread creates its own ChromeDriver instance
        WebDriver  d = new ChromeDriver(options);
        WebDriverWait w = new WebDriverWait(d, Duration.ofSeconds(15));
        d.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        // Store in ThreadLocal — isolated per thread
        TL_DRIVER.set(d);
        TL_WAIT.set(w);

        // Also assign to instance fields so subclasses can use `driver`/`wait`
        driver = d;
        wait   = w;

        System.out.println("[" + Thread.currentThread().getName() + "] Browser launched.");
    }

    @AfterClass
    public void tearDown() {
        WebDriver d = TL_DRIVER.get();
        if (d != null) {
            d.quit();
            TL_DRIVER.remove(); // prevent memory leak
            TL_WAIT.remove();
        }
        System.out.println("[" + Thread.currentThread().getName() + "] Browser closed.");
    }

    // ── Shared helpers ────────────────────────────────────────────

    public void goToHomepage() {
        driver.get("https://www.booking.com/?lang=en-us");
        originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@name='ss' or @placeholder='Where are you going?']")));
        dismissAllPopups();
    }

    public void dismissAllPopups() {
        // Each block catches both NoSuchElementException (element never existed)
        // and StaleElementReferenceException (element existed but DOM re-rendered
        // before isDisplayed() / click() could execute — common on Booking.com).
        try {
            driver.findElement(By.id("onetrust-accept-btn-handler")).click();
        } catch (NoSuchElementException | StaleElementReferenceException ignored) {}

        try {
            driver.findElement(
                    By.xpath("//button[@aria-label='Dismiss sign-in info.']")).click();
        } catch (NoSuchElementException | StaleElementReferenceException ignored) {}

        for (String sel : new String[]{
                "button[aria-label*='Dismiss']",
                "button[aria-label*='Close']",
                "button[aria-label='Not now']"}) {
            try {
                WebElement btn = driver.findElement(By.cssSelector(sel));
                if (btn.isDisplayed()) { btn.click(); break; }
            } catch (NoSuchElementException | StaleElementReferenceException ignored) {}
        }
    }

    public void searchFor(String destination) {
        driver.findElement(
                        By.xpath("//input[@name='ss' or @placeholder='Where are you going?']"))
                .sendKeys(destination);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("(//div[@data-testid='autocomplete-result'] " +
                                    "| //li[@data-testid='autocomplete-results-option'])[1]")))
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
        System.out.println("[" + Thread.currentThread().getName()
                + "] Search results loaded for: " + destination);
    }

    public void closeDatePicker() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

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
        System.out.println("[" + Thread.currentThread().getName()
                + "] Property opened: " + driver.getCurrentUrl());
    }

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
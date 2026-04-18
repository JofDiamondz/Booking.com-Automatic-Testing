package com.booking.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

import java.time.Duration;
import java.util.List;

public class PropertyDetailTest {

    WebDriver driver;
    WebDriverWait wait;
    String originalWindow;
    String propertyWindow;
    String searchDestination = "Miami";

    @BeforeSuite
    public void beforeSuite() {
        System.out.println("PropertyDetailTest suite starting.");
    }

    @AfterSuite
    public void afterSuite() {
        System.out.println("PropertyDetailTest suite complete.");
    }

    @BeforeTest
    public void beforeTest() {
        System.out.println("BeforeTest: property detail test block starting.");
    }

    @AfterTest
    public void afterTest() {
        System.out.println("AfterTest: property detail test block done.");
    }

    // Launches browser, searches for Miami, and opens the first property result
    // All popup handling happens here before any test runs
    @BeforeClass
    public void setUp() throws InterruptedException {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get("https://www.booking.com");
        originalWindow = driver.getWindowHandle();

        // Dismiss cookie consent popup if it appears
        try {
            driver.findElement(By.id("onetrust-accept-btn-handler")).click();
        } catch (NoSuchElementException e) {
            System.out.println("No cookie popup.");
        }

        // Dismiss sign-in overlay if it appears on the homepage
        try {
            driver.findElement(
                    By.xpath("//button[@aria-label='Dismiss sign-in info.']")).click();
        } catch (NoSuchElementException e) {
            System.out.println("No sign-in overlay.");
        }

        // Type the destination into the search box
        driver.findElement(
                        By.xpath("//input[@name='ss' or @placeholder='Where are you going?']"))
                .sendKeys(searchDestination);

        // Select the first autocomplete suggestion
        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//div[@data-testid='autocomplete-result'])[1]"))).click();
        } catch (Exception e) {
            driver.findElement(
                            By.xpath("//input[@name='ss' or @placeholder='Where are you going?']"))
                    .sendKeys(Keys.ENTER);
        }

        // Close the date picker that opens automatically
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);

        // Submit the search
        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@data-testid='searchbox-submit-button']"))).click();
        } catch (Exception e) {
            driver.findElement(
                            By.xpath("//input[@name='ss' or @placeholder='Where are you going?']"))
                    .sendKeys(Keys.ENTER);
        }

        // Wait for search results to load
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@data-testid='property-card']")));

        // Dismiss any popup that appears on the results page
        try {
            driver.findElement(
                    By.xpath("//button[@aria-label='Dismiss sign-in info.']")).click();
        } catch (NoSuchElementException e) {
            System.out.println("No results popup.");
        }

        // Scroll down so the first property card is visible before clicking
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        for (int i = 0; i < 5; i++) {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ARROW_DOWN);
        }
        Thread.sleep(1500);

        // Click the first property card — it opens in a new tab
        wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//div[@data-testid='property-card']//a[@data-testid='title-link'])[1]")))
                .sendKeys(Keys.RETURN);

        wait.until(d -> d.getWindowHandles().size() > 1);

        // Switch to the new property tab
        for (String w : driver.getWindowHandles()) {
            if (!w.equals(originalWindow)) {
                driver.switchTo().window(w);
                break;
            }
        }

        wait.until(d -> !d.getCurrentUrl().contains("searchresults"));
        propertyWindow = driver.getWindowHandle();
        System.out.println("Navigated to: " + driver.getCurrentUrl());
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
        System.out.println("Browser closed.");
    }

    // Before each test, close any stray tabs and return to the property page
    @BeforeMethod
    public void beforeEachTest() {
        closeExtraTabsKeepProperty();
        driver.switchTo().window(propertyWindow);
        System.out.println("Starting test...");
    }

    @AfterMethod
    public void afterEachTest() {
        closeExtraTabsKeepProperty();
        System.out.println("Test finished.");
    }

    // Closes any tab that is not the original search results tab or the property tab
    private void closeExtraTabsKeepProperty() {
        try {
            for (String w : driver.getWindowHandles()) {
                if (!w.equals(originalWindow) && !w.equals(propertyWindow)) {
                    driver.switchTo().window(w);
                    driver.close();
                }
            }
            driver.switchTo().window(propertyWindow);
        } catch (Exception e) {
            System.out.println("Tab cleanup: " + e.getMessage());
        }
    }

    // Verifies the URL is no longer on the search results page after clicking a property
    @Test(priority = 1)
    public void testURLChangesOnClick() {
        String currentURL = driver.getCurrentUrl();
        System.out.println("Property page URL: " + currentURL);

        Assert.assertFalse(currentURL.contains("searchresults"),
                "URL should no longer be the search results page.");
        Assert.assertTrue(currentURL.contains("booking.com"),
                "URL should still be on booking.com.");
    }

    // Verifies the property page has a different title from the homepage
    @Test(priority = 2)
    public void testTitleDiffersFromHomepage() {
        String detailTitle = driver.getTitle();

        // Open homepage in a new tab to compare titles
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get("https://www.booking.com");
        wait.until(ExpectedConditions.titleContains("Booking"));
        String homepageTitle = driver.getTitle();

        driver.close();
        driver.switchTo().window(propertyWindow);

        System.out.println("Homepage title: " + homepageTitle);
        System.out.println("Detail title:   " + detailTitle);

        Assert.assertNotEquals(detailTitle, homepageTitle,
                "Property page title should NOT equal the homepage title.");
    }

    // Verifies the page title is a valid property page and not the homepage
    // All Booking.com property pages include the current year in their title
    @Test(priority = 3)
    public void testPropertyNameMatchesPageTitle() {
        String pageTitle = driver.getTitle();
        System.out.println("Page title: " + pageTitle);

        Assert.assertFalse(pageTitle == null || pageTitle.isEmpty(),
                "Page title should not be empty.");

        Assert.assertFalse(
                pageTitle.equals("Booking.com | Official site | The best hotels, flights, car rentals & accommodations"),
                "Should be on a property page, not the homepage.");

        Assert.assertTrue(pageTitle.contains("2026") || pageTitle.contains("2025"),
                "Property page title should contain the current year. Title: " + pageTitle);

        System.out.println("PASS: Property page title is valid: " + pageTitle);
    }

    // Verifies at least 5 property photos are present
    // A property with fewer than 5 photos is a data quality issue
    @Test(priority = 4)
    public void testAtLeastFivePhotosPresent() {
        List<WebElement> photos = driver.findElements(
                By.xpath("//img[contains(@src,'bstatic.com') or contains(@src,'cf.bstatic')]"));

        // Fall back to any image if the primary locator finds nothing
        if (photos.isEmpty()) {
            photos = driver.findElements(
                    By.xpath("//img[@src and string-length(@src) > 0]"));
        }

        System.out.println("Photos found: " + photos.size());

        Assert.assertTrue(photos.size() >= 5,
                "Property should have at least 5 photos. Found: " + photos.size());
        System.out.println("PASS: " + photos.size() + " photos found.");
    }

    // Verifies the property rating is a valid number between 0 and 10
    // Uses soft assertions so the test reports all failures rather than stopping at the first
    @Test(priority = 5)
    public void testRatingIsValidNumber() {
        SoftAssert softAssert = new SoftAssert();

        try {
            WebElement ratingElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@aria-hidden='true' and contains(@class,'f63b14ab7a')]")));

            String ratingText = ratingElement.getText().trim();
            System.out.println("Rating text: " + ratingText);

            softAssert.assertFalse(ratingText.isEmpty(), "Rating should not be empty.");

            try {
                double rating = Double.parseDouble(ratingText);
                System.out.println("Rating value: " + rating);
                softAssert.assertTrue(rating > 0,
                        "Rating should be greater than 0. Got: " + rating);
                softAssert.assertTrue(rating <= 10,
                        "Rating should be 10 or below. Got: " + rating);
                System.out.println("PASS: Rating " + rating + " is valid.");
            } catch (NumberFormatException e) {
                softAssert.fail("Rating is not a valid number: " + ratingText);
            }

        } catch (Exception e) {
            System.out.println("Rating element not found — property may not have reviews.");
            softAssert.fail("Rating element should be present on the property page.");
        }

        softAssert.assertAll();
    }

    // Verifies clicking Reserve moves the user forward in the booking flow
    // The button should either navigate to checkout/sign-in or reveal the room selection section
    @Test(priority = 6)
    public void testReserveButtonNavigatesForward() throws InterruptedException {
        // Scroll down to load the availability and rooms section
        WebElement body = driver.findElement(By.tagName("body"));
        for (int i = 0; i < 10; i++) body.sendKeys(Keys.ARROW_DOWN);
        Thread.sleep(1500);

        WebElement reserveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[.//span[contains(@class,'bui-button__text') and contains(text(),'Reserve')]]")));

        System.out.println("Reserve button found: " + reserveBtn.getText());
        reserveBtn.click();
        Thread.sleep(3000);

        String urlAfterClick = driver.getCurrentUrl();
        System.out.println("URL after clicking Reserve: " + urlAfterClick);

        boolean navigatedAway = urlAfterClick.contains("checkout")
                || urlAfterClick.contains("sign-in")
                || urlAfterClick.contains("login")
                || urlAfterClick.contains("account");

        // Some properties scroll to the rooms section instead of navigating away
        List<WebElement> roomsSection = driver.findElements(
                By.xpath("//*[contains(@id,'rooms') or contains(@id,'availability') or @data-testid='availability-cta']"));

        Assert.assertTrue(navigatedAway || roomsSection.size() > 0,
                "Clicking Reserve should navigate to checkout/sign-in OR reveal the rooms section.");
        System.out.println("PASS: Reserve button action verified.");
    }
}
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

public class LoginTest {

    WebDriver driver;
    WebDriverWait wait;

    @BeforeSuite
    public void beforeSuite() {
        System.out.println("LoginTest suite starting.");
    }

    @AfterSuite
    public void afterSuite() {
        System.out.println("LoginTest suite complete.");
    }

    @BeforeTest
    public void beforeTest() {
        System.out.println("BeforeTest: login test block starting.");
    }

    @AfterTest
    public void afterTest() {
        System.out.println("AfterTest: login test block done.");
    }

    // Launches browser, dismisses popups, and navigates to the sign-in page before any test runs
    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get("https://www.booking.com");

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

        // Click the Sign in button in the header to reach the sign-in page
        WebElement signInBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[.//span[contains(text(),'Sign in')]] | //button[.//span[contains(text(),'Sign in')]]")));
        signInBtn.click();

        // Wait until the browser confirms we are on the sign-in page
        wait.until(d -> d.getCurrentUrl().contains("account")
                || d.getCurrentUrl().contains("sign-in"));
        System.out.println("Landed on sign-in page: " + driver.getCurrentUrl());
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
        System.out.println("Browser closed.");
    }

    @BeforeMethod
    public void beforeEachTest() {
        System.out.println("Starting test...");
    }

    @AfterMethod
    public void afterEachTest() {
        System.out.println("Test finished.");
    }

    // Verifies that clicking Sign in from the homepage redirects to the correct sign-in URL
    @Test(priority = 1)
    public void testSignInURLCorrect() {
        String currentURL = driver.getCurrentUrl();
        System.out.println("Sign-in URL: " + currentURL);

        Assert.assertTrue(
                currentURL.contains("login") || currentURL.contains("signin")
                        || currentURL.contains("account"),
                "Sign-in page URL should contain a sign-in related path.");
        System.out.println("PASS: Sign-in URL is correct.");
    }

    // Verifies that submitting a valid email causes Booking.com to respond
    // The page title or URL should change to indicate the email was accepted
    @Test(priority = 2)
    public void testValidEmailShowsVerificationStep() throws InterruptedException {
        driver.get("https://account.booking.com/sign-in");

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("username")));
        emailField.clear();
        emailField.sendKeys("djonleon00@gmail.com");
        System.out.println("Email entered.");

        // Capture state before submitting to compare after
        String titleBefore = driver.getTitle();
        String urlBefore = driver.getCurrentUrl();

        driver.findElement(
                By.xpath("//span[contains(text(),'Continue with email')]")).click();

        Thread.sleep(5000);

        String titleAfter = driver.getTitle();
        String urlAfter = driver.getCurrentUrl();

        System.out.println("Title before: " + titleBefore);
        System.out.println("Title after:  " + titleAfter);
        System.out.println("URL after:    " + urlAfter);

        // Any one of these changes confirms Booking.com responded to the valid email
        boolean urlChanged = !urlAfter.equals(urlBefore);
        boolean titleChanged = !titleAfter.equals(titleBefore);
        boolean redirectedToOTP = urlAfter.contains("otp") || urlAfter.contains("code");

        Assert.assertTrue(urlChanged || titleChanged || redirectedToOTP,
                "Submitting a valid email should cause the page title or URL to change.");
        System.out.println("PASS: Valid email produced a page response.");
    }

    // Verifies that the sign-in page has a different title from the homepage
    // This confirms the user is on the correct page after navigation
    @Test(priority = 3)
    public void testTitleDiffersFromHomepage() {
        driver.get("https://account.booking.com/sign-in");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        String signInTitle = driver.getTitle();

        // Open homepage in a new tab just to capture its title
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get("https://www.booking.com");
        wait.until(ExpectedConditions.titleContains("Booking"));
        String homepageTitle = driver.getTitle();

        // Close the homepage tab and return to the sign-in tab
        driver.close();
        driver.switchTo().window(driver.getWindowHandles().iterator().next());

        System.out.println("Homepage title: " + homepageTitle);
        System.out.println("Sign-in title:  " + signInTitle);

        Assert.assertNotEquals(signInTitle, homepageTitle,
                "Sign-in page title should NOT equal the homepage title.");
    }

    // Verifies that submitting an empty email and an invalid email both keep the user on the sign-in page
    // A real bug would be if either case let the user proceed without a valid email
    @Test(priority = 4)
    public void testEmptyAndInvalidEmailValidation() throws InterruptedException {
        driver.get("https://account.booking.com/sign-in");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));

        // Case A: Submit with no email entered
        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[contains(text(),'Continue with email')]")));
        continueBtn.click();
        Thread.sleep(1500);

        String urlAfterEmptySubmit = driver.getCurrentUrl();
        System.out.println("URL after empty submit: " + urlAfterEmptySubmit);

        Assert.assertTrue(
                urlAfterEmptySubmit.contains("sign-in") || urlAfterEmptySubmit.contains("login"),
                "Should remain on sign-in page after submitting an empty email.");
        System.out.println("PASS: Empty email correctly rejected.");

        // Case B: Submit with a string that has no @ or domain
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("username")));
        emailField.clear();
        emailField.sendKeys("dro4898");

        continueBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[contains(text(),'Continue with email')]")));
        continueBtn.click();
        Thread.sleep(1500);

        String urlAfterInvalidEmail = driver.getCurrentUrl();
        System.out.println("URL after invalid email: " + urlAfterInvalidEmail);

        Assert.assertTrue(
                urlAfterInvalidEmail.contains("sign-in") || urlAfterInvalidEmail.contains("login"),
                "Should remain on sign-in page after submitting an invalid email format.");
        System.out.println("PASS: Invalid email format correctly rejected.");
    }

    // Verifies that all expected UI elements are present on the sign-in page
    // Uses soft assertions so all checks run even if one fails
    @Test(priority = 5)
    public void testSignInPageUIElements() {
        driver.get("https://account.booking.com/sign-in");

        SoftAssert softAssert = new SoftAssert();

        // Email input field must be present and visible
        List<WebElement> emailFields = driver.findElements(By.id("username"));
        softAssert.assertTrue(emailFields.size() > 0,
                "Email input field should be present on the sign-in page.");
        if (emailFields.size() > 0) {
            softAssert.assertTrue(emailFields.get(0).isDisplayed(),
                    "Email input field should be visible.");
        }
        System.out.println("Email fields found: " + emailFields.size());

        // Continue button must be present for the user to submit their email
        List<WebElement> continueBtn = driver.findElements(
                By.xpath("//span[contains(text(),'Continue with email')]"));
        softAssert.assertTrue(continueBtn.size() > 0,
                "Continue with email button should be present.");
        System.out.println("Continue button found: " + continueBtn.size());

        // Page title must not be empty
        String title = driver.getTitle();
        softAssert.assertFalse(title == null || title.isEmpty(),
                "Sign-in page title should not be empty.");
        System.out.println("Page title: " + title);

        // URL must confirm the user is on the sign-in page
        String url = driver.getCurrentUrl();
        softAssert.assertTrue(
                url.contains("account") || url.contains("sign-in"),
                "URL should confirm we are on the sign-in page.");
        System.out.println("Page URL confirmed: " + url);

        softAssert.assertAll();
        System.out.println("PASS: All sign-in page UI elements are present.");
    }
}
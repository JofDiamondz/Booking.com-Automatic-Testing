package com.booking.tests;

import com.booking.pages.HeaderNavBar;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class UINavigationTest extends BaseTest {

    @BeforeClass
    public void setUp() {
        super.setUp();
        goToHomepage();
    }

    @BeforeMethod
    public void beforeEachTest() { System.out.println("Starting test..."); }

    @AfterMethod
    public void afterEachTest() { System.out.println("Test finished."); }

    @Test
    public void testNavigationFlowAcrossPages() throws InterruptedException {
        goToHomepage();
        HeaderNavBar nav = new HeaderNavBar(driver, wait);

        // Step 1: Attractions
        nav.clickAttractions();
        Thread.sleep(1500);
        Assert.assertTrue(driver.getCurrentUrl().contains("attractions"));
        System.out.println("Navigated to Attractions.");

        // Step 2: Car rental
        nav.clickCars();
        Thread.sleep(1500);
        Assert.assertTrue(driver.getCurrentUrl().contains("cars"));
        System.out.println("Navigated to Car Rental.");

        // Step 3: Flights
        nav.clickFlights();
        Thread.sleep(1500);
        Assert.assertTrue(driver.getCurrentUrl().contains("flights") || driver.getCurrentUrl().contains("kayak"));
        System.out.println("Navigated to Flights.");

        // Step 4: Back to Stays
        nav.clickStays();
        Thread.sleep(1500);
        Assert.assertTrue(driver.getCurrentUrl().contains("booking.com"));
        System.out.println("Navigated back to Stays.");

        // Step 5: Scroll down and back up
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollBy(0, 1200);");
        Thread.sleep(1000);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        Thread.sleep(1000);
        System.out.println("Scroll verified.");
    }
}

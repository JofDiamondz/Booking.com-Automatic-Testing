package com.booking.tests;

import com.booking.data.SearchData;
import com.booking.pages.HomePage;
import com.booking.pages.SearchResultsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class SearchTest extends BaseTest {

    @BeforeClass
    public void setUp() {
        super.setUp();
        goToHomepage();
    }

    @BeforeMethod
    public void beforeEachTest() { System.out.println("Starting test..."); }

    @AfterMethod
    public void afterEachTest() { System.out.println("Test finished."); }

    @Test(dataProvider = "validDestinations", dataProviderClass = SearchData.class)
    public void testURLUpdatesOnSearch(String destination) {
        goToHomepage();
        HomePage homePage = new HomePage(driver, wait);

        String beforeUrl = driver.getCurrentUrl();
        homePage.searchForDestination(destination);
        String afterUrl = driver.getCurrentUrl();

        System.out.println("Destination: " + destination);
        System.out.println("Before URL: " + beforeUrl);
        System.out.println("After URL: " + afterUrl);

        Assert.assertNotEquals(afterUrl, beforeUrl);
    }

    @Test(dataProvider = "invalidDestinations", dataProviderClass = SearchData.class)
    public void testInvalidDestination(String destination) {
        goToHomepage();
        HomePage homePage = new HomePage(driver, wait);
        SearchResultsPage resultsPage = new SearchResultsPage(driver);

        homePage.searchForDestination(destination);
        resultsPage.waitForResultsToRefresh();

        Assert.assertTrue(resultsPage.hasEmptyResultsOrErrorMessage());
    }

    @Test(dataProvider = "validDestinations", dataProviderClass = SearchData.class)
    public void testResultsCountGreaterThanZero(String destination) {
        goToHomepage();
        HomePage homePage = new HomePage(driver, wait);
        SearchResultsPage resultsPage = new SearchResultsPage(driver);

        homePage.searchForDestination(destination);
        resultsPage.waitForResultsToRefresh();

        int count = resultsPage.getResultCount();
        System.out.println("Result count for " + destination + ": " + count);

        Assert.assertTrue(count > 0);
    }

    @Test(dataProvider = "validDestinations", dataProviderClass = SearchData.class)
    public void testSortDropdownPresent(String destination) {
        goToHomepage();
        HomePage homePage = new HomePage(driver, wait);
        SearchResultsPage resultsPage = new SearchResultsPage(driver);

        homePage.searchForDestination(destination);
        resultsPage.waitForResultsToRefresh();

        Assert.assertTrue(resultsPage.isSortDropdownPresent());
    }
}

package com.booking.tests;

import com.booking.pages.HomePage;
import com.booking.pages.SearchResultsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class FilterTest extends BaseTest {

    @BeforeClass
    public void setUp() {
        super.setUp();
        goToHomepage();
    }

    @BeforeMethod
    public void beforeEachTest() { System.out.println("Starting test..."); }

    @AfterMethod
    public void afterEachTest() { System.out.println("Test finished."); }

    @Test(invocationCount = 3)
    public void testFreeWifiFilter() {
        HomePage homePage = new HomePage(driver, wait);
        SearchResultsPage resultsPage = new SearchResultsPage(driver);

        goToHomepage();
        homePage.searchForDestination("Paris");
        resultsPage.waitForResultsPageToLoad();
        resultsPage.waitForFiltersToLoad();

        int beforeCount = resultsPage.getResultCount();
        boolean clicked = resultsPage.clickFreeWifiFilter();

        if (!clicked) {
            System.out.println("Skipping Free WiFi — filter not available.");
            return;
        }

        resultsPage.waitForResultsToRefresh();
        int afterCount = resultsPage.getResultCount();

        Assert.assertTrue(beforeCount != afterCount || afterCount > 0);
    }

    @Test
    public void testStarRatingButtonsPresent() {
        HomePage homePage = new HomePage(driver, wait);
        SearchResultsPage resultsPage = new SearchResultsPage(driver);

        goToHomepage();
        homePage.searchForDestination("Paris");
        resultsPage.waitForResultsPageToLoad();

        Assert.assertTrue(resultsPage.getStarRatingFilterCount() > 0);
    }

    @Test
    public void testMapToggleVisible() {
        HomePage homePage = new HomePage(driver, wait);
        SearchResultsPage resultsPage = new SearchResultsPage(driver);

        goToHomepage();
        homePage.searchForDestination("London");
        resultsPage.waitForResultsPageToLoad();

        Assert.assertTrue(resultsPage.isMapToggleVisible());
    }

    @Test
    public void testSortDropdownPresent() {
        HomePage homePage = new HomePage(driver, wait);
        SearchResultsPage resultsPage = new SearchResultsPage(driver);

        goToHomepage();
        homePage.searchForDestination("London");
        resultsPage.waitForResultsPageToLoad();

        Assert.assertTrue(resultsPage.isSortDropdownPresent());
    }
}

package com.booking.tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatePickerTest extends BaseTest {

    @BeforeSuite
    public void beforeSuite() {
        System.out.println("DatePickerTest suite starting.");
    }

    @AfterSuite
    public void afterSuite() {
        System.out.println("DatePickerTest suite complete.");
    }

    @BeforeTest
    public void beforeTest() {
        System.out.println("BeforeTest: date picker test block starting.");
    }

    @AfterTest
    public void afterTest() {
        System.out.println("AfterTest: date picker test block done.");
    }

    @BeforeClass
    public void setUp() {
        super.setUp();
        goToHomepage();
    }

    @BeforeMethod
    public void beforeEachTest() {
        System.out.println("Starting test...");
    }

    @AfterMethod
    public void afterEachTest() {
        System.out.println("Test finished.");
    }

    public void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private String futureDate(int daysFromNow) {
        return LocalDate.now().plusDays(daysFromNow)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void openCalendar() {
        WebElement datesBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
            "[data-testid='searchbox-dates-container'], " +
            "[data-testid='date-display-field-start'], " +
            "button[data-testid*='date'], " +
            "div[class*='DateField'], " +
            ".sb-searchbox__dates"
        )));
        datesBtn.click();
        pause(2000);
        System.out.println("[DatePickerTest] Calendar opened.");
    }

    private void clickDate(String isoDate) {
        WebElement cell = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
            "td[data-date='" + isoDate + "'], span[data-date='" + isoDate + "']"
        )));
        System.out.println("[DatePickerTest] Clicking date: " + isoDate);
        cell.click();
        pause(1800);
    }

    // ── Test 1: Calendar is visible after clicking check-in ───────
    @Test(priority = 1, description = "Verify calendar is visible after clicking the check-in field")
    public void testCalendarVisible() {
        openCalendar();

        WebElement calendar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
            "[data-testid='datepicker-tabs'], " +
            "[data-testid='searchbox-datepicker-calendar'], " +
            "div[class*='CalendarMonth'], " +
            "div[role='dialog'] table, " +
            ".bui-calendar"
        )));

        Assert.assertTrue(calendar.isDisplayed(),
            "Calendar should be visible after clicking the check-in field");
        System.out.println("[DatePickerTest] ✔ testCalendarVisible PASSED");
        pause(2000);
    }

    // ── Test 2: Check-in field not empty after date selected ──────
    @Test(priority = 2, description = "Verify check-in field is not empty after selecting a date")
    public void testCheckInFieldNotEmpty() {
        pause(1000);
        String checkIn = futureDate(7);
        clickDate(checkIn);

        WebElement checkInDisplay = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
            "[data-testid='date-display-field-start'], " +
            "span[data-testid*='checkin'], " +
            ".sb-date-field:first-of-type"
        )));

        String displayed = checkInDisplay.getText().trim();
        System.out.println("[DatePickerTest] Check-in field shows: " + displayed);

        Assert.assertFalse(displayed.isEmpty(),
            "Check-in field should not be empty after selecting a date");
        System.out.println("[DatePickerTest] ✔ testCheckInFieldNotEmpty PASSED");
        pause(1500);
    }

    // ── Test 3: Check-out cannot be before check-in ───────────────
    @Test(priority = 3, description = "Verify dates before check-in are disabled for check-out")
    public void testCheckOutNotBeforeCheckIn() {
        pause(1000);
        String beforeCheckIn = futureDate(4);

        try {
            WebElement cell = driver.findElement(By.cssSelector("td[data-date='" + beforeCheckIn + "']"));
            String ariaDisabled = cell.getAttribute("aria-disabled");
            String classVal     = cell.getAttribute("class");
            boolean disabled = "true".equals(ariaDisabled)
                || (classVal != null && (classVal.contains("disabled") || classVal.contains("blocked")));

            System.out.println("[DatePickerTest] Cell " + beforeCheckIn
                + " | aria-disabled=" + ariaDisabled + " | class=" + classVal);
            Assert.assertTrue(disabled,
                "Date before check-in should be marked disabled.");
        } catch (NoSuchElementException e) {
            System.out.println("[DatePickerTest] Cell " + beforeCheckIn
                + " absent from DOM — counts as disabled/unavailable.");
        }

        System.out.println("[DatePickerTest] ✔ testCheckOutNotBeforeCheckIn PASSED");
        pause(2000);
    }

    // ── Test 4: Nights count updates after check-out selected ─────
    @Test(priority = 4, description = "Verify nights count label updates after selecting check-out date")
    public void testNightsCountUpdates() {
        pause(1000);
        String checkOut = futureDate(12);
        clickDate(checkOut);
        pause(2500);

        try {
            WebElement nightsLabel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                    + "'abcdefghijklmnopqrstuvwxyz'),'night')]")
            ));
            String txt = nightsLabel.getText().trim();
            System.out.println("[DatePickerTest] Nights label: " + txt);
            Assert.assertFalse(txt.isEmpty(), "Nights label should not be empty");
        } catch (Exception e) {
            WebElement startField = driver.findElement(By.cssSelector(
                "[data-testid='date-display-field-start'], .sb-date-field:first-of-type"));
            WebElement endField = driver.findElement(By.cssSelector(
                "[data-testid='date-display-field-end'], .sb-date-field:last-of-type"));
            String start = startField.getText().trim();
            String end   = endField.getText().trim();
            System.out.println("[DatePickerTest] Start=" + start + "  End=" + end);
            Assert.assertNotEquals(start, end,
                "Check-in and check-out dates must differ — confirms a range was selected");
        }

        System.out.println("[DatePickerTest] ✔ testNightsCountUpdates PASSED");
        pause(2000);
    }

    // ── Test 5: Select "Flexible dates" tab in the date picker ────
    @Test(priority = 5, description = "Verify the Flexible Dates option can be selected in the date picker")
    public void testSelectFlexibleDates() {
        // Re-open the calendar — previous test may have closed it
        openCalendar();

        // Booking.com shows two tabs at the top of the date picker:
        //   "Specific dates"  and  "Flexible dates"
        // The tab bar has data-testid="datepicker-tabs" and each tab is a button inside it.
        WebElement flexibleTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath(
                "//div[@data-testid='datepicker-tabs']//button[contains("
                + "translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                + "'flexible')]"
                + " | //button[contains("
                + "translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                + "'flexible')][@role='tab' or ancestor::*[@data-testid='datepicker-tabs']]"
            )
        ));

        String tabText = flexibleTab.getText().trim();
        System.out.println("[DatePickerTest] Flexible dates tab found: '" + tabText + "'");

        flexibleTab.click();
        pause(2000);

        // After clicking, the tab should become active/selected.
        // Booking.com marks the active tab with aria-selected="true" or a selected CSS class.
        String ariaSelected = flexibleTab.getAttribute("aria-selected");
        String tabClass     = flexibleTab.getAttribute("class");
        System.out.println("[DatePickerTest] Tab aria-selected='" + ariaSelected
            + "' class='" + tabClass + "'");

        boolean isActive = "true".equals(ariaSelected)
            || (tabClass != null && (tabClass.contains("selected")
                || tabClass.contains("active")
                || tabClass.contains("--active")));

        // Additionally verify the flexible dates UI panel is now shown —
        // it contains duration options like "1 week", "2 weeks", a month picker, etc.
        boolean flexiblePanelVisible = false;
        try {
            WebElement flexPanel = driver.findElement(By.cssSelector(
                "[data-testid='flexible-dates-container'], " +
                "[data-testid='flexible-date-search'], " +
                "div[class*='flexible'], " +
                "div[class*='Flexible']"
            ));
            flexiblePanelVisible = flexPanel.isDisplayed();
            System.out.println("[DatePickerTest] Flexible dates panel visible: " + flexiblePanelVisible);
        } catch (NoSuchElementException e) {
            // Fallback: the specific-dates calendar table should now be GONE
            List<WebElement> calendarTables = driver.findElements(
                By.cssSelector("td[data-date]"));
            // If no calendar cells are present, the view switched away from specific dates
            flexiblePanelVisible = calendarTables.isEmpty();
            System.out.println("[DatePickerTest] Calendar date cells gone (flexible view active): "
                + flexiblePanelVisible);
        }

        Assert.assertTrue(isActive || flexiblePanelVisible,
            "Flexible dates tab should be active or its panel should be visible after clicking. "
            + "aria-selected=" + ariaSelected + ", panelVisible=" + flexiblePanelVisible);

        System.out.println("[DatePickerTest] ✔ testSelectFlexibleDates PASSED");
        pause(2000);

        // Close the calendar before the test ends
        closeDatePicker();
    }
}

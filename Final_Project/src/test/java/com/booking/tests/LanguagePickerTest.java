package com.booking.tests;

import com.booking.pages.LanguagePicker;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class LanguagePickerTest extends BaseTest {

    @BeforeClass
    public void setUp() {
        super.setUp();
        goToHomepage();
    }

    @BeforeMethod
    public void beforeEachTest() {
        goToHomepage(); // fresh homepage before each test
        System.out.println("Starting test...");
    }

    @AfterMethod
    public void afterEachTest() { System.out.println("Test finished."); }

    @Test
    public void testCloseLanguagePicker() {
        LanguagePicker picker = new LanguagePicker(driver, wait);
        picker.openLanguagePicker();
        picker.closeLanguagePicker();

        Assert.assertFalse(picker.isModalVisible());
        System.out.println("Language picker closed successfully.");
    }

    @Test
    public void testSelectMultipleLanguages() {
        LanguagePicker picker = new LanguagePicker(driver, wait);
        picker.selectMultipleLanguages("Français", "Deutsch", "Español", "العربية");

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("ar"));
        System.out.println("Multiple languages selected. Final URL: " + url);
    }
}

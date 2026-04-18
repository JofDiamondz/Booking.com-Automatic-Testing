package com.booking.data;

import org.testng.annotations.DataProvider;

public class SearchData {

    @DataProvider(name = "validDestinations")
    public Object[][] validDestinations() {
        return new Object[][]{
                {"Paris"},
                {"London"}
        };
    }

    @DataProvider(name = "invalidDestinations")
    public Object[][] invalidDestinations() {
        return new Object[][]{
                {"fakeplace123456789"},
                {"zzzznotarealcity"},
                {"@@@###invalid"}
        };
    }
}
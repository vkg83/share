package com.vkg.finance.share.stock.client;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public interface WebScrapper<T> {
    String CHROMEDRIVER_LOCATION = "C:\\Users\\ADMIN\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe";

    T scrap();

    default WebDriver getWebDriver() {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_LOCATION);
        var driver = new ChromeDriver();
        driver.manage().window().maximize();
        return driver;
    }
}

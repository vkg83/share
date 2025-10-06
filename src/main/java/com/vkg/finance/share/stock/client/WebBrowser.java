package com.vkg.finance.share.stock.client;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.function.Function;

public class WebBrowser {
    private static final String CHROMEDRIVER_LOCATION = "C:\\Users\\ADMIN\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe";

    public static <T> T execute(Function<WebDriver, T> fn) {
        WebDriver webDriver = null;
        try {
            webDriver = getWebDriver();
            return fn.apply(webDriver);
        } finally {
            if (webDriver != null) {
                webDriver.close();
                webDriver.quit();
            }
        }
    }

    private static WebDriver getWebDriver() {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_LOCATION);
        var driver = new ChromeDriver();
        driver.manage().window().maximize();
        return driver;
    }
}

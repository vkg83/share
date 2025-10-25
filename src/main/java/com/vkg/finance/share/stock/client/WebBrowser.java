package com.vkg.finance.share.stock.client;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.function.Function;

public class WebBrowser {
    private static final String CHROMEDRIVER_LOCATION = "C:\\Users\\ADMIN\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe";

    public static <T> T execute(Function<WebDriver, T> fn) {
        return execute(false, fn);
    }
    public static <T> T execute(boolean background, Function<WebDriver, T> fn) {
        WebDriver webDriver = null;
        try {
            webDriver = getWebDriver(background);
            return fn.apply(webDriver);
        } finally {
            if (webDriver != null) {
                webDriver.quit();
            }
        }
    }

    private static WebDriver getWebDriver(boolean background) {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_LOCATION);
        ChromeOptions options = new ChromeOptions();
        if(background) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--enable-features=ClipboardContentSetting");
        }
        var driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1920, 1080));
        return driver;
    }
}

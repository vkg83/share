package com.vkg.finance.share.stock.client;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChartInkClient implements WebScrapper<List<String>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChartInkClient.class);
    private static final By COPY_BUTTON = By.xpath("//div/span[text()='Copy']");
    private static final By TABLE_BUTTON = By.xpath("//div/span//span[text()='table']");
    private static final By TABLE = By.xpath("//table/tbody/tr/td/span[text()='1']");
    private static final By COPY_MODAL = By.xpath("//span[text()='Table data copied successfully']");

    private final String scanName;

    public ChartInkClient(String scanName) {
        this.scanName = scanName;
    }

    @Override
    public List<String> scrap() {
        String clipboardText = WebBrowser.execute(driver -> {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = toolkit.getSystemClipboard();
            clipboard.setContents(new StringSelection(""), null);
            String url = "https://chartink.com/screener/" + scanName;
            driver.get(url);
            var wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(TABLE));
            var el = wait.until(ExpectedConditions.elementToBeClickable(COPY_BUTTON));
            el.click();
            el = wait.until(ExpectedConditions.elementToBeClickable(TABLE_BUTTON));
            el.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(COPY_MODAL));
            try {
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            } catch (Exception ex) {
                LOGGER.error("Not able to get copied text", ex);
            }
            return "";
        });

        return parse(clipboardText);
    }

    private List<String> parse(String text) {
        String[] rows = text.split("\n");
        List<String> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            var row = rows[i];
            String[] values = row.split("\t");
            list.add(values[2]);
        }
        LOGGER.info("Found {} stocks in ChartInk Scan {}", list.size(), scanName);
        LOGGER.info("{}", list);
        return list;
    }
}

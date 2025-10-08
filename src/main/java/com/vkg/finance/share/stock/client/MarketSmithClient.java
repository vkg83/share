package com.vkg.finance.share.stock.client;

import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

@Component
public class MarketSmithClient implements WebScrapper<List<StockInfo>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketSmithClient.class);
    private static final Map<String, String> BSE_TO_NSE = Map.of("GVTD", "GVT&D");


    private final List<String> symbols;
    private final HashMap<String, String> ignored;

    public MarketSmithClient(List<String> symbols) {
        this.symbols = symbols;
        ignored = new HashMap<>();
    }

    public List<String> getIgnored() {
        return new ArrayList<>(ignored.keySet());
    }

    @Override
    public List<StockInfo> scrap() {
        var list = WebBrowser.execute(driver -> {
            List<StockInfo> infoList = new ArrayList<>();
            for (int i = 0; i < symbols.size(); i++) {
                var symbol = symbols.get(i);
                symbol = BSE_TO_NSE.getOrDefault(symbol, symbol);
                try {
                    var info = fetchInfo(driver, symbol);
                    infoList.add(info);
                    LOGGER.info("Processed ({}/{}): {} - {}", i + 1, symbols.size(), symbol, info.getAverageVolume());
                } catch (Exception e) {
                    ignored.put(symbol, e.getMessage());
                }
            }
            return infoList;
        });

        if (!ignored.isEmpty()) {
            LOGGER.warn("Failed for {}: {}", ignored.size(), ignored.keySet());
        }

        return list;
    }

    private StockInfo fetchInfo(WebDriver driver, String symbol) {
        searchSymbol(driver, symbol);
        StockInfo info = new StockInfo(symbol);
        WebDriverWait wait1 = new WebDriverWait(driver, Duration.ofSeconds(10));
        var el = wait1.until(ExpectedConditions.visibilityOfElementLocated(By.id("evaluation_strength_placeholder")));
        var el1 = wait1.until(ExpectedConditions.elementToBeClickable(By.id("consolidated")));

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el1);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        info.setEpsStrength(getInt(el, "epsStrength"));
        info.setPriceStrength(getInt(el, "priceStrength"));
        info.setBuyerDemand(getText(el, "buyerDemand"));
        info.setGroupRank(getInt(el, "groupRank"));
        info.setGroupId(findGroupId(driver));
        var el2 = driver.findElement(By.id("detailsInitial2"));
        info.setYearlyEps(findYearly(el2.findElement(By.id("fundamntlEarningTableId"))));
        var el3 = driver.findElement(By.id("quarterlyEarningsCat")).findElement(By.id("formattedSalesAndEarningTable"));
        info.setQuarterlyEpsGrowth(findQuarterly(2, el3));
        info.setQuarterlySalesGrowth(findQuarterly(4, el3));
        List<BigDecimal> masterDetails = findDetails(el2.findElement(By.id("details_placeholder_masterscore")), 0);
        info.setMasterScore(masterDetails.get(0));
        List<BigDecimal> details = findDetails(el2.findElement(By.id("details_placeholder_eps")), 0, 1, 4);
        info.setEpsGrowth(details.get(0));
        info.setEpsStability(details.get(1));
        info.setReturnOnEquity(details.get(2));
        info.setNetMargin(findRatio(driver.findElement(By.id("keyRatioTable"))));
        var els = driver.findElements(By.cssSelector("#redFlags_placeholder td.surveillanceflag > i.redFlag"));
        info.setRedFlags(els.size());
        info.setAverageVolume(find50SmaVolume(driver));
        return info;
    }

    private BigDecimal find50SmaVolume(WebDriver driver) {
        WebElement btn = driver.findElement(By.id("enlargeBtnClick"));
        btn.click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        var el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id=\"enlargeGraphHeaderInfo\"]/div/div[3]/div[2]/div/div[2]/p[1]")));
        return getBigDecimal(el.getText());
    }

    @NotNull
    private static String findGroupId(WebDriver driver) {
        var groupEl = driver.findElement(By.cssSelector("#detailsGroupName a"));
        String href = Objects.requireNonNullElse(groupEl.getAttribute("href"), "");
        var items = href.split("/");
        return items[items.length - 2];
    }

    private static void searchSymbol(WebDriver driver, String symbol) {
        driver.get("https://marketsmithindia.com/mstool/eval/" + symbol.toLowerCase() + "/evaluation.jsp");
    }

    private List<BigDecimal> findRatio(WebElement keyRatioTable) {
        var rows = keyRatioTable.findElements(By.tagName("tr"));
        List<BigDecimal> list = new ArrayList<>();
        for (var row : rows) {
            var cols = row.findElements(By.tagName("td"));
            if (cols.isEmpty() || !cols.get(0).getText().contains("After Tax Margin (%)")) {
                continue;
            }
            for (int i = 1; i < cols.size(); i++) {
                WebElement col = cols.get(i);
                list.add(getBigDecimal(col.getText().trim()));
            }
        }
        return list;
    }

    private List<BigDecimal> findDetails(WebElement el, int... indices) {
        var rows = el.findElements(By.cssSelector("div.value"));
        List<BigDecimal> list = new ArrayList<>();
        for (int index : indices) {
            String text = rows.get(index).getText().trim();
            list.add(getBigDecimal(text));
        }

        return list;
    }

    private List<BigDecimal> findYearly(WebElement el) {
        var rows = el.findElements(By.tagName("tr"));
        List<BigDecimal> list = new ArrayList<>();
        for (var row : rows) {
            var cols = row.findElements(By.tagName("td"));
            if (cols.isEmpty() || isNotInteger(cols.get(0).getText().trim())) {
                continue;
            }
            list.add(getBigDecimal(cols.get(1).getText().trim()));
        }
        return list;
    }

    private List<BigDecimal> findQuarterly(int colIndex, WebElement el) {
        var rows = el.findElements(By.tagName("tr"));
        List<BigDecimal> list = new ArrayList<>();
        for (var row : rows) {
            var cols = row.findElements(By.tagName("td"));
            if (row.getText().isBlank() || cols.isEmpty() || isNotInteger(cols.get(0).getText().trim().substring(4))) {
                continue;
            }
            String value = cols.get(colIndex).getText().trim();
            if (value.endsWith("%")) {
                value = value.substring(0, value.length() - 1);
            }
            list.add(getBigDecimal(value));
        }
        return list;
    }

    private static String getText(WebElement el, String var) {
        return el.findElement(By.xpath("//div[@data-target='#" + var + "']/h3/span[last()]")).getText();
    }

    private static int getInt(WebElement el, String var) {
        return Integer.parseInt(getText(el, var));
    }

    private static boolean isNotInteger(String value) {
        try {
            Integer.parseInt(value);
        } catch (Exception ex) {
            return true;
        }
        return false;
    }

    private static BigDecimal getBigDecimal(String value) {
        if (value == null || value.isBlank() || "N/A".equals(value)) {
            return null;
        }
        value = value.endsWith("%") ? value.substring(0, value.length() - 1) : value;
        return new BigDecimal(value.replaceAll(",", ""));
    }
}

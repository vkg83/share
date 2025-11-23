package com.vkg.finance.share.stock.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

class MarketSmithClientTest {

    public static final Path BASE_PATH = Path.of("C:\\Users\\ADMIN\\Documents\\Stock Analysis");
    public static final Path DOWNLOAD_DIR = Path.of("C:\\Users\\ADMIN\\Downloads");
    public static final String GROUP_FILE = "industryGroupList.csv";
    public static final String INSIDE_PIVOT_FILE = "Filter_India_Stocks.csv";
    public static final String REACHING_PIVOT_FILE = "Filter_India_Stocks (1).csv";
    private static List<String> insideBar;
    private static LocalDate today;

    @BeforeAll
    static void checkGroupFileDate() throws IOException {
        today = LocalDate.now();
        verifyLatestFile(GROUP_FILE);
        verifyLatestFile(INSIDE_PIVOT_FILE);
        verifyLatestFile(REACHING_PIVOT_FILE);
        var chartInk = new ChartInkClient("inside-bar-611639");
        insideBar = chartInk.scrap().stream().map(ChartInkModel::getSymbol).toList();
    }

    private static void verifyLatestFile(String file) throws IOException {
        var time = Files.getLastModifiedTime(DOWNLOAD_DIR.resolve(file));
        var fileDate = time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Assertions.assertEquals(today, fileDate, "File is not up-to-date: " + file);
    }

    @ParameterizedTest
    @CsvFileSource(files = "C:\\Users\\ADMIN\\Documents\\zerodha-secret\\users.txt")
    void shouldPaintPortfolioData(String userName) {
        List<String> symbols = ZerodhaClient.getHoldings(userName);
        symbols = symbols.stream().filter(s -> !ZerodhaClient.etfs.contains(s)).toList();
        var infoList = getStockInfos(symbols);
        var fileName = "Portfolio Analysis " + today + " " + userName + ".xlsx";
        var outputPath = BASE_PATH.resolve(fileName);
        new MarketSmithExcelPainter(outputPath).writeFile(infoList);
    }

    @ParameterizedTest
    @CsvSource({
            "super-performance-stocks-11,Daily Analysis",
            "super-performance-stocks-low-liquidity,Low Liquidity Analysis",
            "combined-scanner-inside-bar,Combined Winners"})
    void shouldPaintChartInkAnalysisData(String scanName, String filePrefix) {
        var chartInk = new ChartInkClient(scanName);
        var symbols = chartInk.scrap().stream().map(ChartInkModel::getSymbol).toList();
        List<StockInfo> info = getStockInfos(symbols);
        var fileName = filePrefix + " " + today + ".xlsx";
        var outputPath = BASE_PATH.resolve(fileName);
        new MarketSmithExcelPainter(outputPath).writeFile(info);
    }

    private static List<StockInfo> getStockInfos(List<String> symbols) {
        List<StockInfo> info = new ArrayList<>();
        int retryCount = 10;
        do {
            var unit = new MarketSmithClient(symbols);
            info.addAll(unit.scrap());
            symbols = unit.getIgnored();
        } while (!symbols.isEmpty() && --retryCount > 0);

        for (var s : symbols) {
            System.out.println("Ignored: " + s);
        }
        return info.stream().map(MarketSmithClientTest::enrichInsideBar).toList();
    }

    private static StockInfo enrichInsideBar(StockInfo info) {
        info.setInsideBar(insideBar.contains(info.getSymbol()));
        return info;
    }
}
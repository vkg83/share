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

    private static final String BASE_PATH = "C:\\Users\\ADMIN\\Documents\\Stock Analysis";
    public static final Path GROUP_FILE = Path.of("C:\\Users\\ADMIN\\Downloads\\industryGroupList.csv");

    @BeforeAll
    static void checkGroupFileDate() throws IOException {
        var time = Files.getLastModifiedTime(GROUP_FILE);
        var fileDate = time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Assertions.assertEquals(LocalDate.now(), fileDate,"Group File is not up-to-date");
    }

    @ParameterizedTest
    @CsvFileSource(files = "C:\\Users\\ADMIN\\Documents\\zerodha-secret\\users.txt")
    void shouldPaintPortfolioData(String userName) {
        List<String> symbols = ZerodhaClient.getHoldings(userName);
        symbols = symbols.stream().filter(s -> !ZerodhaClient.etfs.contains(s)).toList();
        var infoList = getStockInfos(symbols);
        var outputPath = Path.of(BASE_PATH, "Portfolio Analysis " + LocalDate.now() + " " + userName + ".xlsx");
        new MarketSmithExcelPainter(outputPath).writeFile(infoList);
    }

    @ParameterizedTest
    @CsvSource({
            "super-performance-stocks-11,Daily Analysis",
            "super-performance-stocks-low-liquidity,Low Liquidity Analysis",
            "rsi-crossover-28043067,Rsi Crossover"})
    void shouldPaintChartInkAnalysisData(String scanName, String filePrefix) {
        var chartInk = new ChartInkClient(scanName);
        var symbols = chartInk.scrap();
        List<StockInfo> info = getStockInfos(symbols);

        var outputPath = Path.of(BASE_PATH, filePrefix + " " + LocalDate.now() + ".xlsx");
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
        return info;
    }
}
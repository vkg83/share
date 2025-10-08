package com.vkg.finance.share.stock.client;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class ChartInkClientTest {

    @Test
    void shouldGetChartInkData() {
        var chartInk = new ChartInkClient("super-performance-stocks-11");
        var infoList = chartInk.scrap();
        System.out.println(infoList);
    }

    @Test
    void shouldAlertForVolume() throws InterruptedException {
        var symbols = new ArrayList<String>();
        symbols.addAll(loadSymbols("Daily Analysis"));
        symbols.addAll(loadSymbols("Low Liquidity Analysis"));
        System.out.println("Total " + symbols.size() + " symbols to alert.");
        for (int i = 0; i < 100; i++) {
            var vol = new ChartInkClient("super-performance-stocks-volume").scrap();
            vol.retainAll(symbols);

            if (vol.isEmpty()) {
                System.out.println("Nothing found");
            } else {
                System.out.println(vol);
            }
            Thread.sleep(300000);
        }
    }

    private List<String> loadSymbols(String filePrefix) {
        var daPath = Path.of(MarketSmithClientTest.BASE_PATH, filePrefix + " " + LocalDate.now() + ".xlsx");
        List<String> symbols = new MarketSmithExcelPainter(daPath).readFile().stream().map(StockInfo::getSymbol).toList();
        System.out.println(symbols.size() + " symbols in " + filePrefix + ": " + symbols);
        return symbols;
    }
}
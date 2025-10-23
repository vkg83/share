package com.vkg.finance.share.stock.client;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class ChartInkClientTest {

    @Test
    void shouldGetChartInkData() {
        var chartInk = new ChartInkClient("super-performance-stocks-11");
        var infoList = chartInk.scrap();
        System.out.println(infoList);
    }

    @Test
    void shouldAlertForVolume() throws InterruptedException {
        var symbolMap = new HashMap<String, StockInfo>();
        var visited = new HashMap<String, Integer>();
        symbolMap.putAll(loadSymbols("Daily Analysis"));
        symbolMap.putAll(loadSymbols("Low Liquidity Analysis"));
        System.out.println("Total " + symbolMap.size() + " symbols to alert.");
        var start = LocalTime.of(9, 20);
        for (int i = 0; i < 100; i++) {
            List<String> vol;
            LocalTime now = LocalTime.now();
            var mi = BigDecimal.valueOf(Duration.between(start, now).toMinutes() / 360.0);
            try {
                vol = new ChartInkClient("super-performance-stocks-volume")
                        .scrap().stream()
                        .sorted(Comparator.comparing(ChartInkModel::getVolume).reversed())
                        .filter(m -> symbolMap.containsKey(m.getSymbol()))
                        .filter(m -> {
                            var sy = symbolMap.get(m.getSymbol());
                            var v = sy.getAverageVolume().multiply(mi).longValue();
                            return v < m.getVolume();
                        }).map(ChartInkModel::getSymbol).toList();
            } catch (Exception ex) {
                System.out.println("Error: "+ ex.getMessage());
                vol = new ArrayList<>();
            }

            if (now.isAfter(start) && !vol.isEmpty()) {
                for(var key: vol) {
                    int count = visited.getOrDefault(key, 0);
                    visited.put(key, count + 1);
                    if(count == 0) {
                        System.out.println("New: " + key);
                    }
                }
            }

            if(!visited.isEmpty()) {
                System.out.println("Old: " + visited);
            }

            Thread.sleep(300000);
        }
    }

    private Map<String, StockInfo> loadSymbols(String filePrefix) {
        var daPath = Path.of(MarketSmithClientTest.BASE_PATH, filePrefix + " " + LocalDate.now() + ".xlsx");
        var symbols = new MarketSmithExcelPainter(daPath).readFile().stream()
                .collect(Collectors.toMap(StockInfo::getSymbol, Function.identity()));
        System.out.println(symbols.size() + " symbols in " + filePrefix + ": " + symbols.keySet());
        return symbols;
    }
}
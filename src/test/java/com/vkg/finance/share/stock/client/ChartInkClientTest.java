package com.vkg.finance.share.stock.client;

import com.vkg.finance.share.WhatsappClient;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
    void shouldAlertForVolume() throws InterruptedException, ExecutionException {
        var symbolMap = new HashMap<String, StockInfo>();
        var visited = new HashSet<String>();
        symbolMap.putAll(loadSymbols("Daily Analysis"));
        symbolMap.putAll(loadSymbols("Low Liquidity Analysis"));
        System.out.println("Total " + symbolMap.size() + " symbols to alert.");
        var wa = new WhatsappClient();
        wa.init();
        var start = LocalTime.of(9, 20);
        for (int i = 0; i < 100; i++) {
            List<String> vol;
            try {
                vol = new ChartInkClient("super-performance-stocks-volume")
                        .scrap().stream().filter(symbolMap::containsKey).toList();
            } catch (Exception ex) {
                System.out.println("Error: "+ ex.getMessage());
                vol = new ArrayList<>();
            }

            if(!visited.isEmpty()) {
                System.out.println("Old: " + visited);
            }

            if (LocalTime.now().isAfter(start) && !vol.isEmpty()) {
                System.out.println("New: " + vol);
                wa.send("919766045435", Objects.toString(vol));
                vol.forEach(symbolMap::remove);
                visited.addAll(vol);
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
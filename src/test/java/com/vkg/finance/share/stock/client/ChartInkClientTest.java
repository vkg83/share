package com.vkg.finance.share.stock.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.io.ClassPathResource;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class ChartInkClientTest {
    private static final LocalDate today = LocalDate.now();
    private final LocalTime start = LocalTime.of(9, 0);
    private final LocalTime end = LocalTime.of(15, 30);

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

        for (int i = 0; i < 100; i++) {
            List<String> vol;
            LocalTime now = LocalTime.now();
            try {
                vol = new ChartInkClient("super-performance-stocks-volume").scrap().stream()
                        .sorted(Comparator.comparing(ChartInkModel::getVolume).reversed())
                        .map(ChartInkModel::getSymbol)
                        .filter(symbolMap::containsKey).toList();
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
                vol = new ArrayList<>();
            }

            visited.keySet().retainAll(vol);

            if (!visited.isEmpty()) {
                var map = visited.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                System.out.println("Old: " + map);
            }

            List<String> newStocks = new ArrayList<>();
            if (now.isAfter(start.plusMinutes(20)) && !vol.isEmpty()) {
                for (var key : vol) {
                    int count = visited.getOrDefault(key, 0);
                    visited.put(key, count + 1);
                    if (count == 0) {
                        newStocks.add(key);
                    }
                }
            }

            if (!newStocks.isEmpty()) {
                play();
                System.out.println("New: " + newStocks);
            }

            if (now.isBefore(start) || now.isAfter(end)) {
                System.out.println("Terminating...");
                break;
            }

            Thread.sleep(300000);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "super-performance-stocks-11,Daily Analysis",
            "super-performance-stocks-low-liquidity,Low Liquidity Analysis",
            "combined-scanner-inside-bar,Combined Winners",
            "rsi-crossover-28043067,Rsi Crossover"})
    void shouldComparePreviousDay(String scanName, String filePrefix) {
        Map<String, StockInfo> prevDaySymbol = loadSymbols(filePrefix, today.minusDays(1));
        Map<String, StockInfo> infoList = loadSymbols(filePrefix, today);
        List<String> newSymbols = new ArrayList<>();
        for (var info : infoList.keySet()) {
            StockInfo existing = prevDaySymbol.remove(info);
            if (existing == null) {
                newSymbols.add(info);
            }
        }

        System.out.println(filePrefix + " (" + scanName + ")");
        if (!prevDaySymbol.isEmpty()) {
            System.out.println("\tRemoved: " + prevDaySymbol.keySet());
        }
        if (!newSymbols.isEmpty()) {
            System.out.println("\tNew: " + newSymbols);
        }
    }

    private static Map<String, StockInfo> loadSymbols(String filePrefix, LocalDate date) {
        var daPath = Path.of(MarketSmithClientTest.BASE_PATH, filePrefix + " " + date + ".xlsx");
        return new MarketSmithExcelPainter(daPath).readFile().stream()
                .collect(Collectors.toMap(StockInfo::getSymbol, Function.identity()));
    }


    private static Map<String, StockInfo> loadSymbols(String filePrefix) {
        var symbols = loadSymbols(filePrefix, LocalDate.now());
        System.out.println(symbols.size() + " symbols in " + filePrefix + ": " + symbols.keySet());
        return symbols;
    }

    private static void play() {
        var r = new ClassPathResource("sample-12s.wav");
        try (var is = AudioSystem.getAudioInputStream(r.getInputStream())) {
            Clip clip = AudioSystem.getClip();
            clip.open(is);
            clip.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
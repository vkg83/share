package com.vkg.finance.share.stock.client;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class ChartInkClientTest {
    private final LocalDate today = LocalDate.now();
    private final int days = DayOfWeek.SATURDAY.getValue() - today.getDayOfWeek().getValue();
    private final LocalTime start = LocalTime.of(9, 15);
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
            var mi = Duration.between(start, now).toMinutes() / 240.0;
            try {
                vol = new ChartInkClient("super-performance-stocks-volume").scrap().stream()
                        .sorted(Comparator.comparing(ChartInkModel::getVolume).reversed())
                        .filter(m -> symbolMap.containsKey(m.getSymbol()))
                        .filter(m -> hasCrossed(m, symbolMap.get(m.getSymbol()), mi))
                        .map(ChartInkModel::getSymbol).toList();
            } catch (Exception ex) {
                System.out.println("Error: "+ ex.getMessage());
                vol = new ArrayList<>();
            }

            visited.keySet().retainAll(vol);

            if(!visited.isEmpty()) {
                var map = visited.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                System.out.println("Old: " + map);
            }

            List<String> newStocks = new ArrayList<>();
            if (now.isAfter(start.plusMinutes(5)) && !vol.isEmpty()) {
                for(var key: vol) {
                    int count = visited.getOrDefault(key, 0);
                    visited.put(key, count + 1);
                    if(count == 0) {
                        newStocks.add(key);
                    }
                }
            }

            if(!newStocks.isEmpty()) {
                play();
                System.out.println("New: " + newStocks);
            }

            if(now.isBefore(start) || now.isAfter(end)) {
                System.out.println("Terminating...");
                break;
            }

            Thread.sleep(300000);
        }
    }

    private boolean hasCrossed(ChartInkModel m, StockInfo stockInfo, double mi) {
        BigDecimal averageWeeklyVolume = stockInfo.getAverageWeeklyVolume();
        BigDecimal existingWeeklyVol = today.getDayOfWeek() == DayOfWeek.MONDAY ? BigDecimal.ZERO : stockInfo.getWeeklyVolume();
        long remainingWeeklyVol = averageWeeklyVolume.subtract(existingWeeklyVol).longValue();
        var avgDayVolume = remainingWeeklyVol / days;
        long dayVolume = today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY ? 0: m.getVolume();
        return dayVolume > remainingWeeklyVol || dayVolume > avgDayVolume * 1.4 || dayVolume > avgDayVolume * mi;
    }

    private Map<String, StockInfo> loadSymbols(String filePrefix) {
        var daPath = Path.of(MarketSmithClientTest.BASE_PATH, filePrefix + " " + LocalDate.now() + ".xlsx");
        var symbols = new MarketSmithExcelPainter(daPath).readFile().stream()
                .collect(Collectors.toMap(StockInfo::getSymbol, Function.identity()));
        System.out.println(symbols.size() + " symbols in " + filePrefix + ": " + symbols.keySet());
        return symbols;
    }

    private static void play() {
        var r = new ClassPathResource("sample-12s.wav");
        try (var is = AudioSystem.getAudioInputStream(r.getInputStream())){
            Clip clip = AudioSystem.getClip();
            clip.open(is);
            clip.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
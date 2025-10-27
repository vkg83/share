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
        var date = LocalDate.now();
        var start = LocalTime.of(9, 15);
        var end = LocalTime.of(15, 30);
        for (int i = 0; i < 100; i++) {
            List<String> vol;
            LocalTime now = LocalTime.now();
            var mi = Duration.between(start, now).toMinutes() / 240.0;
            try {
                vol = new ChartInkClient("super-performance-stocks-volume")
                        .scrap().stream()
                        .sorted(Comparator.comparing(ChartInkModel::getVolume).reversed())
                        .filter(m -> symbolMap.containsKey(m.getSymbol()))
                        .filter(m -> {
                            var sy = symbolMap.get(m.getSymbol());
                            BigDecimal avgVolume = sy.getAverageWeeklyVolume();
                            BigDecimal existingVol = date.getDayOfWeek() == DayOfWeek.MONDAY ? BigDecimal.ZERO : sy.getWeeklyVolume();
                            var requiredVolume = avgVolume.subtract(existingVol).longValue();
                            long dayVolume = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY ? 0: m.getVolume();
                            boolean volCrossed = dayVolume > requiredVolume || dayVolume > requiredVolume * mi;
                            if(!volCrossed) {
                                visited.remove(m.getSymbol());
                            }
                            return volCrossed;
                        }).map(ChartInkModel::getSymbol).toList();
            } catch (Exception ex) {
                System.out.println("Error: "+ ex.getMessage());
                vol = new ArrayList<>();
            }

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
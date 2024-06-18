package com.vkg.finance.share.stock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@ConfigurationProperties("data")
public class MarketConfig {
    private List<LocalDate> holidays;

    public List<LocalDate> getHolidays() {
        return holidays;
    }

    public void setHolidays(Map<String, String> yearMap) {
        holidays = new ArrayList<>();
        for (var year : yearMap.keySet()) {
            holidays.addAll(extractDates(year, yearMap.get(year)));
        }
    }

    private Set<LocalDate> extractDates(String year, String holidayList) {
        return Arrays.stream(holidayList.split(","))
                .map(s -> LocalDate.parse(year + "-" + s.trim()))
                .collect(Collectors.toSet());
    }

    public boolean isMarketClosed(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || holidays.contains(date);
    }

    public boolean isMarketOpen(LocalDate date) {
        return !isMarketClosed(date);
    }
}

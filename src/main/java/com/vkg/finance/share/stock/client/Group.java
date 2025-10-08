package com.vkg.finance.share.stock.client;

@SuppressWarnings("unused")
public class Group {
    private final String id;
    private final String name;
    private final int stockCount;
    private final int currentRank;
    private final int lastWeekRank;
    private final int last3MonthRank;
    private final int last6MonthRank;

    public Group(String[] line) {
        id = line[1];
        name = line[2];
        stockCount = toInt(line[3], 0);
        currentRank = toInt(line[4], 500);
        lastWeekRank = toInt(line[5], 500);
        last3MonthRank = toInt(line[6], 500);
        last6MonthRank = toInt(line[7], 500);
    }

    private static int toInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStockCount() {
        return stockCount;
    }

    public int getCurrentRank() {
        return currentRank;
    }

    public int getLastWeekRank() {
        return lastWeekRank;
    }

    public int getLast3MonthRank() {
        return last3MonthRank;
    }

    public int getLast6MonthRank() {
        return last6MonthRank;
    }
}

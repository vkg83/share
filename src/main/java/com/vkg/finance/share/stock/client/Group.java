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
        stockCount = Integer.parseInt(line[3]);
        currentRank = Integer.parseInt(line[4]);
        lastWeekRank = Integer.parseInt(line[5]);
        last3MonthRank = Integer.parseInt(line[6]);
        last6MonthRank = Integer.parseInt(line[7]);
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

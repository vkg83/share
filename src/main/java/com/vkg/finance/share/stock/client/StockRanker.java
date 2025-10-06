package com.vkg.finance.share.stock.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

public record StockRanker(StockInfo stockInfo) {

    public boolean isQtrEpsGrowthGE(int percent) {
        BigDecimal latestQEpsGrowth = stockInfo.getQuarterlyEpsGrowth().get(0);
        return isGE(latestQEpsGrowth, BigDecimal.valueOf(percent));
    }

    private static boolean isGE(BigDecimal d1, BigDecimal d2) {
        return d1.compareTo(d2) >= 0;
    }

    private static boolean isLE(BigDecimal d1, BigDecimal d2) {
        return d1.compareTo(d2) <= 0;
    }


    public boolean isQtrEpsGrowthAcc(int count) {
        List<BigDecimal> epsGrowth = stockInfo.getQuarterlyEpsGrowth();
        BigDecimal latest = epsGrowth.get(0);
        for (int i = 1; i < count; i++) {
            var current = epsGrowth.get(i);
            if (!isGE(latest, current)) {
                return false;
            }
            latest = current;
        }
        return true;
    }

    public boolean isYrEpsGrowthGE(int percent) {
        BigDecimal latestQEpsGrowth = stockInfo.getEpsGrowth();
        return isGE(latestQEpsGrowth, BigDecimal.valueOf(percent));
    }

    public boolean isStabilityLE(int value) {
        BigDecimal epsStability = stockInfo.getEpsStability();
        return isLE(epsStability, BigDecimal.valueOf(value));
    }

    public boolean isYrEpsPositive(int count) {
        List<BigDecimal> epsGrowth = stockInfo.getYearlyEps();
        BigDecimal latest = epsGrowth.get(0);
        for (int i = 1; i < count; i++) {
            var current = epsGrowth.get(i);
            if (!isGE(latest, current)) {
                return false;
            }
            latest = current;
        }
        return true;
    }

    public boolean isYrEpsAllTimeHigh() {
        List<BigDecimal> epsGrowth = stockInfo.getYearlyEps();
        BigDecimal latest = epsGrowth.get(0);
        for (int i = 1; i < epsGrowth.size(); i++) {
            var current = epsGrowth.get(i);
            if (!isGE(latest, current)) {
                return false;
            }
        }
        return true;
    }

    public boolean isQGTY() {
        return !isLE(stockInfo.getQuarterlyEpsGrowth().get(0), stockInfo.getEpsGrowth());
    }

    public boolean isQtrSalesGrowthGE(int percent) {
        var value = stockInfo.getQuarterlySalesGrowth().get(0);
        return isGE(value, BigDecimal.valueOf(percent));
    }

    public boolean isQtrSalesGrowthAcc(int count) {
        List<BigDecimal> salesGrowth = stockInfo.getQuarterlySalesGrowth();
        BigDecimal latest = salesGrowth.get(0);
        for (int i = 1; i < count; i++) {
            var current = salesGrowth.get(i);
            if (!isGE(latest, current)) {
                return false;
            }
            latest = current;
        }
        return true;
    }

    public boolean isRoeGE(int percent) {
        return isGE(stockInfo.getReturnOnEquity(), BigDecimal.valueOf(percent));
    }

    public boolean isNetMarginYoYExpanding(int index) {
        var margin = stockInfo.getNetMargin();
        BigDecimal latest = margin.get(0);
        for (int i = 1; i <= index; i++) {
            var current = margin.get(i);
            if (!isGE(latest, current)) {
                return false;
            }
            latest = current;
        }
        return true;
    }

    public boolean isNetMarginExpanding(int index) {
        var margin = stockInfo.getNetMargin();
        BigDecimal latest = margin.get(0);
        var current = margin.get(index);
        return isGE(latest, current);
    }

    public boolean isNetMarginAllTimeHigh() {
        List<BigDecimal> margin = stockInfo.getNetMargin();
        BigDecimal latest = margin.get(0);
        for (int i = 1; i < margin.size(); i++) {
            var current = margin.get(i);
            if (!isGE(latest, current)) {
                return false;
            }
        }
        return true;
    }

    public static Integer rank(Supplier<Boolean> flag) {
        try {
            return flag.get() ? 1 : 0;
        } catch (Exception ex) {
            return null;
        }
    }

    @SafeVarargs
    public static Integer rankAll(Supplier<Boolean>... flags) {
        Integer finalValue = null;
        for (var flag: flags) {
            var value = rank(flag);
            if(value != null) {
                if (finalValue == null) {
                    finalValue = value;
                } else {
                    finalValue += value;
                }
            }
        }
        return finalValue;
    }
}

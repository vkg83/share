package com.vkg.finance.share.stock.model;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;

import java.util.function.Function;

public class FundWrapper implements Comparable<FundWrapper> {
    private final FundInfo info;
    private final BarSeries series;
    private final NumericIndicator indicator;

    public FundWrapper(FundInfo info, BarSeries series, Function<BarSeries, NumericIndicator> fn) {
        this.series = series;
        this.info = info;
        this.indicator = fn.apply(series);
    }

    @Override
    public String toString() {
        final int index = indicator.getBarSeries().getEndIndex();
        return String.format("| %-10s | %7.4f%%", info.getSymbol(), indicator.getValue(index).doubleValue());
    }

    @Override
    public int compareTo(FundWrapper o) {
        final Num value = indicator.getValue(indicator.getBarSeries().getEndIndex());
        final Num oValue = o.indicator.getValue(o.indicator.getBarSeries().getEndIndex());
        return value.compareTo(oValue);
    }

    public BarSeries getSeries() {
        return series;
    }

    public FundInfo getInfo() {
        return info;
    }

}

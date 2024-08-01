package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundWrapper;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.numeric.NumericIndicator;

public class MAPurchaseStrategy extends GenericSelectionStrategy {
    public static final int MIN_VOLUME = 10000;

    public MAPurchaseStrategy(MarketDataProvider dataProvider) {
        super(dataProvider);
    }

    @Override
    int getHistoryDays() {
        return 20;
    }

    @Override
    boolean isSelected(FundInfo info) {
        return true;
    }

    @Override
    boolean isSelected(FundWrapper wrapper) {
        var close = NumericIndicator.closePrice(wrapper.getSeries());
        var preClose = NumericIndicator.of(close.previous());
        Rule r = NumericIndicator.volume(wrapper.getSeries()).isGreaterThan(MIN_VOLUME)
                .and(close.isLessThan(close.sma(20))).and(close.isGreaterThan(preClose))
                .and(preClose.isGreaterThan(preClose.previous()));

        return r.isSatisfied(wrapper.getSeries().getEndIndex());
    }

    @Override
    NumericIndicator getRanker(BarSeries series) {
        var close = NumericIndicator.closePrice(series);
        var sma = close.sma(20);
        var diff = close.minus(sma);

        return diff.multipliedBy(100).dividedBy(sma);
    }
}

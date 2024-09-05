package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundWrapper;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.numeric.NumericIndicator;

public class MASaleStrategy extends GenericSelectionStrategy {

    public MASaleStrategy(MarketDataProvider dataProvider) {
        super(dataProvider);
    }

    @Override
    protected int getHistoryDays() {
        return 20;
    }

    @Override
    protected boolean isSelected(FundInfo info) {
        return true;
    }

    @Override
    protected boolean isSelected(FundWrapper wrapper) {
        var close = NumericIndicator.closePrice(wrapper.getSeries());
        var preClose = NumericIndicator.of(close.previous());
        Rule r = close.isGreaterThan(close.sma(20))
                .and(close.isLessThan(preClose))
                .and(preClose.isLessThan(preClose.previous()));

        return r.isSatisfied(wrapper.getSeries().getEndIndex());
    }

    @Override
    protected NumericIndicator getRanker(BarSeries series) {
        var close = NumericIndicator.closePrice(series);
        var sma = close.sma(20);
        var diff = sma.minus(close);

        return diff.multipliedBy(100).dividedBy(sma);
    }
}

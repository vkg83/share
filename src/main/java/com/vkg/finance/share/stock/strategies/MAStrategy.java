package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundWrapper;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;

public class MAStrategy extends GenericSelectionStrategy {
    public static final int MIN_VOLUME = 10000;

    public MAStrategy(MarketDataProvider dataProvider) {
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
        //BollingerBandFacade fc = new BollingerBandFacade(wrapper.getSeries(), 20, 2);
        Rule r = NumericIndicator.volume(wrapper.getSeries()).isGreaterThan(MIN_VOLUME)
                .and(close.isLessThan(close.sma(20))).and(close.isGreaterThan(close.previous()));

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

package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundWrapper;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.numeric.NumericIndicator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GenericSelectionStrategy extends AbstractSelectionStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericSelectionStrategy.class);
    private final MarketDataProvider dataProvider;

    public GenericSelectionStrategy(MarketDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    protected abstract int getHistoryDays();

    @Override
    protected List<FundInfo> execute(List<FundInfo> allFunds, LocalDate date) {
        List<FundWrapper> fundList = new ArrayList<>();
        int historyDays = getHistoryDays();
        for (var info : allFunds) {
            if(!isSelected(info)) continue;
            final List<FundHistory> history = dataProvider.getHistory(info.getSymbol(), date, historyDays);
            if(history.size() < historyDays) continue;
            final List<Bar> bars = history.stream()
                    .sorted(Comparator.comparing(FundHistory::getDate))
                    .map(FundHistory::toBar).collect(Collectors.toList());
            BarSeries series = new BaseBarSeriesBuilder().withName(info.getSymbol()).withBars(bars)
                    .build();
            fundList.add(new FundWrapper(info, series, this::getRanker));
        }
        fundList = fundList.stream().filter(this::isSelected).sorted().collect(Collectors.toList());
        fundList.forEach(f -> LOGGER.info("{}", f));
        return fundList.stream().map(FundWrapper::getInfo).collect(Collectors.toList());
    }

    protected abstract boolean isSelected(FundInfo info);
    protected abstract boolean isSelected(FundWrapper wrapper);
    protected abstract NumericIndicator getRanker(BarSeries series);
}

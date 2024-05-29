package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.client.FundDataProvider;
import com.vkg.finance.share.stock.model.Fund;
import com.vkg.finance.share.stock.model.FundHistory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DarvosTradingStrategy extends AbstractTradingStrategy {

    private FundDataProvider dataProvider;
    private Frequency frequency = Frequency.WEEKLY;

    public DarvosTradingStrategy(FundDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    protected boolean execute(Fund fund) {
        final LocalDate now = LocalDate.now();
        LocalDate boxEnd = now.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));
        LocalDate boxStart = boxEnd.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        var history = dataProvider.getHistory(fund, boxStart, boxEnd);
        double high = findHigh(history);
        return fund.getLastTradingPrice() >= high;
    }

    private double findHigh(List<FundHistory> history) {
        final FundHistory other = new FundHistory();
        other.setLastTradedPrice(Double.MAX_VALUE);
        return history.stream().max(Comparator.comparing(FundHistory::getLastTradedPrice))
                .orElse(other).getLastTradedPrice();
    }
}

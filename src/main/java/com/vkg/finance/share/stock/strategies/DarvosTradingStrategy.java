package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.client.FundDataProvider;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundHistory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DarvosTradingStrategy extends AbstractTradingStrategy {

    private final FundDataProvider dataProvider;

    public DarvosTradingStrategy(FundDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    protected boolean execute(FundInfo fundInfo) {
        final LocalDate now = LocalDate.now();
        LocalDate boxEnd = now.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));
        LocalDate boxStart = boxEnd.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        var history = dataProvider.getHistory(fundInfo.getSymbol(), boxStart, boxEnd);
        final Optional<FundHistory> h = dataProvider.getHistory(fundInfo.getSymbol(), now);
        if(h.isEmpty()) return false;
        var price = h.get().getClosingPrice();
        double high = findHigh(history);
        return price >= high;
    }

    private double findHigh(List<FundHistory> history) {
        final FundHistory other = new FundHistory();
        other.setClosingPrice(Double.MAX_VALUE);
        return history.stream().max(Comparator.comparing(FundHistory::getClosingPrice))
                .orElse(other).getClosingPrice();
    }
}

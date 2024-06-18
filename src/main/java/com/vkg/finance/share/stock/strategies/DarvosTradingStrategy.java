package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.repository.MarketDataProvider;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DarvosTradingStrategy extends AbstractTradingStrategy {

    private final MarketDataProvider dataProvider;
    private LocalDate currentDate;

    public DarvosTradingStrategy(MarketDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.currentDate = LocalDate.now();
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    @Override
    protected FundHistory execute(FundInfo fundInfo) {
        LocalDate boxEnd = currentDate.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));
        LocalDate boxStart = boxEnd.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        var history = dataProvider.getHistory(fundInfo.getSymbol(), boxStart, boxEnd);
        final Optional<FundHistory> h = dataProvider.getHistory(fundInfo.getSymbol(), currentDate);
        if(h.isEmpty()) return null;
        var price = h.get().getClosingPrice();
        double high = findHigh(history);
        return price >= high ? h.get() : null;
    }

    private double findHigh(List<FundHistory> history) {
        final FundHistory other = new FundHistory();
        other.setHighPrice(Double.MAX_VALUE);
        return history.stream().max(Comparator.comparing(FundHistory::getHighPrice))
                .orElse(other).getHighPrice();
    }
}

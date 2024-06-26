package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.repository.MarketDataProvider;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RedGreenStrategy extends AbstractTradingStrategy {

    private MarketDataProvider dataProvider;
    private Frequency frequency = Frequency.WEEKLY;

    public RedGreenStrategy(MarketDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    protected FundHistory execute(FundInfo fundInfo) {
        final LocalDate now = LocalDate.now();
        var history = dataProvider.getHistory(fundInfo.getSymbol(), now.minusYears(1),now);
        var high = findHigh(history);
        var low = findLow(history);
        final boolean b = high.isPresent() && low.isPresent() && high.get().getDate().isBefore(low.get().getDate());
        return null;
    }

    private Optional<FundHistory> findHigh(List<FundHistory> history) {
        return history.stream().max(Comparator.comparing(FundHistory::getHighPrice));
    }
    private Optional<FundHistory> findLow(List<FundHistory> history) {
        return history.stream().min(Comparator.comparing(FundHistory::getLowPrice));
    }
}

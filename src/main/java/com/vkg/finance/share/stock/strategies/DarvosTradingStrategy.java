package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.client.FundDataProvider;
import com.vkg.finance.share.stock.model.Fund;
import com.vkg.finance.share.stock.model.FundHistory;

import java.util.Comparator;
import java.util.List;

public class DarvosTradingStrategy extends AbstractTradingStrategy {

    private FundDataProvider dataProvider;
    private Frequency frequency;
    @Override
    protected boolean execute(Fund fund) {
        var history = dataProvider.getHistory(fund);
        double high = findHigh(history);
        return fund.getLastTradingPrice() >= high;
    }

    private double findHigh(List<FundHistory> history) {
        return history.stream().max(Comparator.comparing(FundHistory::getLastTradedPrice)).orElseThrow().getLastTradedPrice();
    }
}

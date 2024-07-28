package com.vkg.finance.share.stock.trade;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.strategies.SelectionStrategy;

import java.time.LocalDate;
import java.util.List;

public abstract class AbstractTradeModel implements TradeModel {
    private SelectionStrategy strategy;

    public void setStrategy(SelectionStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public final void trade(List<FundInfo> source, LocalDate date) {
        if(strategy != null) {
            source = strategy.select(source, date);
        }

        performTrade(source, date);
    }

    protected abstract void performTrade(List<FundInfo> source, LocalDate date);
}

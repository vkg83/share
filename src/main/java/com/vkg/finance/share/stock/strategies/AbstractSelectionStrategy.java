package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundInfo;

import java.time.LocalDate;
import java.util.List;

public abstract class AbstractSelectionStrategy implements SelectionStrategy {
    private SelectionStrategy next;

    public final SelectionStrategy setNext(SelectionStrategy next) {
        this.next = next;
        return this;
    }

    @Override
    public final List<FundInfo> select(List<FundInfo> fundInfos, LocalDate date) {
        var filteredFunds = this.execute(fundInfos, date);
        if(next != null) {
            filteredFunds = next.select(filteredFunds, date);
        }
        return filteredFunds;
    }

    protected abstract List<FundInfo> execute(List<FundInfo> fundInfos, LocalDate date);

    public static final AbstractSelectionStrategy PASS = new AbstractSelectionStrategy() {
        protected List<FundInfo> execute(List<FundInfo> fundInfos, LocalDate date) {
            return fundInfos;
        }
    };
}

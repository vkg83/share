package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundInfo;

import java.util.List;

public abstract class AbstractSelectionStrategy implements SelectionStrategy {
    SelectionStrategy next;

    public final void setNext(SelectionStrategy next) {
        this.next = next;
    }

    @Override
    public final List<FundInfo> select(List<FundInfo> fundInfos) {
        var filteredFunds = this.execute(fundInfos);
        if(next != null) {
            filteredFunds = next.select(filteredFunds);
        }
        return filteredFunds;
    }

    protected abstract List<FundInfo> execute(List<FundInfo> fundInfos);
}

package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.Fund;

import java.util.List;

public abstract class AbstractSelectionStrategy implements SelectionStrategy {
    SelectionStrategy next;

    public final void setNext(SelectionStrategy next) {
        this.next = next;
    }

    @Override
    public final List<Fund> select(List<Fund> funds) {
        var filteredFunds = this.execute(funds);
        if(next != null) {
            filteredFunds = next.select(filteredFunds);
        }
        return filteredFunds;
    }

    protected abstract List<Fund> execute(List<Fund> funds);
}

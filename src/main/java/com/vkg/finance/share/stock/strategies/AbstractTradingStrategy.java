package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.Fund;

public abstract class AbstractTradingStrategy implements TradingStrategy {
    TradingStrategy next;

    public final void setNext(TradingStrategy next) {
        this.next = next;
    }

    @Override
    public final boolean buy(Fund fund) {
        var flag = this.execute(fund);
        if(flag & next != null) {
            flag = next.buy(fund);
        }
        return flag;
    }

    protected abstract boolean execute(Fund fund);
}

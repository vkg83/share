package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundInfo;

public abstract class AbstractTradingStrategy implements TradingStrategy {
    TradingStrategy next;

    public final void setNext(TradingStrategy next) {
        this.next = next;
    }

    @Override
    public final boolean buy(FundInfo fundInfo) {
        var flag = this.execute(fundInfo);
        if(flag & next != null) {
            flag = next.buy(fundInfo);
        }
        return flag;
    }

    protected abstract boolean execute(FundInfo fundInfo);
}

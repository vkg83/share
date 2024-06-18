package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.model.FundInfo;

public abstract class AbstractTradingStrategy implements TradingStrategy {
    TradingStrategy next;

    public final void setNext(TradingStrategy next) {
        this.next = next;
    }

    @Override
    public final FundHistory buy(FundInfo fundInfo) {
        var flag = this.execute(fundInfo);
        if(flag == null & next != null) {
            flag = next.buy(fundInfo);
        }
        return flag;
    }

    protected abstract FundHistory execute(FundInfo fundInfo);
}

package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundInfo;

public interface TradingStrategy {
    boolean buy(FundInfo fundInfo);
}

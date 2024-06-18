package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.model.FundInfo;

public interface TradingStrategy {
    FundHistory buy(FundInfo fundInfo);
}

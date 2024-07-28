package com.vkg.finance.share.stock.trade;

import com.vkg.finance.share.stock.model.FundInfo;

import java.time.LocalDate;
import java.util.List;

public interface TradeModel {
    void trade(List<FundInfo> source, LocalDate date);
}

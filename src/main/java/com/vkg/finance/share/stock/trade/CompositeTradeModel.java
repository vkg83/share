package com.vkg.finance.share.stock.trade;

import com.vkg.finance.share.stock.model.FundInfo;

import java.time.LocalDate;
import java.util.List;

public class CompositeTradeModel implements TradeModel {
    private List<TradeModel> tradeModelList;

    public CompositeTradeModel(TradeModel... tradeModels) {
        this.tradeModelList = List.of(tradeModels);
    }

    @Override
    public void trade(List<FundInfo> source, LocalDate date) {
        tradeModelList.forEach(t -> t.trade(source, date));
    }
}

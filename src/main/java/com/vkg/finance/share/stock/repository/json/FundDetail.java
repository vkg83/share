package com.vkg.finance.share.stock.repository.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.repository.MarketDataProviderImpl;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundDetail {
    private PriceInfo priceInfo;

    public void setPriceInfo(PriceInfo priceInfo) {
        this.priceInfo = priceInfo;
    }

    public FundHistory toFundHistory(String symbol, LocalDate date) {
        final FundHistory history = new FundHistory();
        history.setSymbol(symbol);
        history.setDate(date);
        history.setOpeningPrice(priceInfo.open);
        history.setClosingPrice(priceInfo.intraDayHighLow.get("value"));
        history.setHighPrice(priceInfo.intraDayHighLow.get("max"));
        history.setLowPrice(priceInfo.intraDayHighLow.get("min"));
        history.setLastTradedPrice(priceInfo.intraDayHighLow.get("value"));
        return history;
    }
}

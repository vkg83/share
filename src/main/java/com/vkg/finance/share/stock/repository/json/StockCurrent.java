package com.vkg.finance.share.stock.repository.json;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vkg.finance.share.stock.model.FundHistory;

import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StockCurrent {

    private List<StockCurrentHistory> data;

    public List<FundHistory> getData() {
        return data == null? null:data.stream().map(StockCurrentHistory::toFundHistory).collect(Collectors.toList());
    }

    public void setData(List<StockCurrentHistory> data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockCurrentHistory {
        private String symbol;
        @JsonProperty("open")
        private double openingPrice;
        @JsonProperty("dayHigh")
        private double highPrice;
        @JsonProperty("dayLow")
        private double lowPrice;
        @JsonProperty("lastPrice")
        private double lastTradedPrice;
        @JsonProperty("totalTradedVolume")
        private long volume;

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public void setOpeningPrice(double openingPrice) {
            this.openingPrice = openingPrice;
        }

        public void setHighPrice(double highPrice) {
            this.highPrice = highPrice;
        }

        public void setLowPrice(double lowPrice) {
            this.lowPrice = lowPrice;
        }

        public void setLastTradedPrice(double lastTradedPrice) {
            this.lastTradedPrice = lastTradedPrice;
        }

        public void setVolume(long volume) {
            this.volume = volume;
        }

        public FundHistory toFundHistory() {
            FundHistory history = new FundHistory();
            history.setSymbol(symbol);
            history.setOpeningPrice(openingPrice);
            history.setClosingPrice(lastTradedPrice);
            history.setHighPrice(highPrice);
            history.setLowPrice(lowPrice);
            history.setLastTradedPrice(lastTradedPrice);
            history.setVolume(volume);
            return history;
        }
    }
}

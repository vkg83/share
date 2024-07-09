package com.vkg.finance.share.stock.repository.json;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.model.FundInfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EtfCurrent {

    private List<EtfCurrentHistory> data;

    public List<FundHistory> getData() {
        return data == null? null:data.stream().map(EtfCurrentHistory::toFundHistory).collect(Collectors.toList());
    }

    public void setData(List<EtfCurrentHistory> data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EtfCurrentHistory {
        private String symbol;
        @JsonProperty("open")
        private double openingPrice;
        @JsonProperty("high")
        private double highPrice;
        @JsonProperty("low")
        private double lowPrice;
        @JsonProperty("ltP")
        private double lastTradedPrice;
        @JsonProperty("qty")
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

package com.vkg.finance.share.stock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundHistory {
//    CH_SYMBOL=NIFTYBEES,
//    CH_SERIES=EQ,
//    CH_MARKET_TYPE=N,
//    CH_TIMESTAMP=2024-05-16,
//    TIMESTAMP=2024-05-15T18:30:00.000Z,
//    CH_TRADE_HIGH_PRICE=248.5,
//    CH_TRADE_LOW_PRICE=244.91,
//    CH_OPENING_PRICE=247.99,
//    CH_CLOSING_PRICE=248.18,
//    CH_LAST_TRADED_PRICE=248.49,
//    CH_PREVIOUS_CLS_PRICE=246.42,
//    CH_TOT_TRADED_QTY=5522327,
//    CH_TOT_TRADED_VAL=1.36106234626E9,
//    CH_52WEEK_HIGH_PRICE=254.69,
//    CH_52WEEK_LOW_PRICE=198.25,
//    CH_TOTAL_TRADES=38602,
//    CH_ISIN=INF204KB14I2,
//    createdAt=2024-05-16T12:00:32.745Z,
//    updatedAt=2024-05-16T12:00:32.745Z,
//    __v=0,
//    SLBMH_TOT_VAL=null,
//    VWAP=246.47,
//    mTIMESTAMP=16-May-2024

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @JsonProperty("CH_LAST_TRADED_PRICE")
    private double lastTradedPrice;
    @JsonProperty("CH_TIMESTAMP")
    private LocalDate date;
    @JsonProperty("CH_TOT_TRADED_QTY")
    private long volume;

    public double getLastTradedPrice() {
        return lastTradedPrice;
    }

    public void setLastTradedPrice(double lastTradedPrice) {
        this.lastTradedPrice = lastTradedPrice;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = LocalDate.parse(date, FMT);
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return date + ": " + lastTradedPrice + " - " + volume ;
    }
}

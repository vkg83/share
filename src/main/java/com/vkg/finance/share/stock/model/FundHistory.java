package com.vkg.finance.share.stock.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

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
    @JsonAlias({"CH_SYMBOL", "symbol"})
    private String symbol;
    @JsonAlias({"CH_OPENING_PRICE", "open"})
    private double openingPrice;
    @JsonAlias({"CH_CLOSING_PRICE", "ltP", "lastPrice"})
    private double closingPrice;
    @JsonAlias({"CH_TRADE_HIGH_PRICE", "high", "dayHigh"})
    private double highPrice;
    @JsonAlias({"CH_TRADE_LOW_PRICE", "low", "dayLow"})
    private double lowPrice;
    @JsonAlias({"CH_LAST_TRADED_PRICE", "ltP", "lastPrice"})
    private double lastTradedPrice;
    @JsonAlias("CH_TIMESTAMP")
    private LocalDate date;
    @JsonAlias({"CH_TOT_TRADED_QTY", "qty", "totalTradedVolume"})
    private long volume;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getOpeningPrice() {
        return openingPrice;
    }

    public void setOpeningPrice(double openingPrice) {
        this.openingPrice = openingPrice;
    }

    public double getClosingPrice() {
        return closingPrice;
    }

    public void setClosingPrice(double closingPrice) {
        this.closingPrice = closingPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public double getLastTradedPrice() {
        return lastTradedPrice;
    }

    public void setLastTradedPrice(double lastTradedPrice) {
        this.lastTradedPrice = lastTradedPrice;
        this.closingPrice = lastTradedPrice;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(String date) {
        setDate(LocalDate.parse(date, FMT));
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public void adjust(FundInfo fundInfo) {
        symbol = fundInfo.getSymbol();

        if (fundInfo.getActionDate() != null && date.isBefore(fundInfo.getActionDate())) {
            final double factor = getFactor(fundInfo.getCorporateAction());
            openingPrice *= factor;
            closingPrice *= factor;
            highPrice *= factor;
            lowPrice *= factor;
            lastTradedPrice *= factor;
        }
    }

    private double getFactor(String actionStr) {
        var p = Pattern.compile("FACE VALUE SPLIT \\(SUB-DIVISION\\) - FROM R[SE] (\\d+.?\\d*)/- PER SHARE TO R[ES] (\\d+.?\\d*)/- PER SHARE");
        var m = p.matcher(actionStr);
        double factor;
        if (m.matches()) {
            double n = Double.parseDouble(m.group(2));
            double d = Double.parseDouble(m.group(1));
            factor = n / d;
        } else {
            throw new RuntimeException("Not able to adjust history for " + symbol + " on " + date);
        }

        return factor;
    }


    @Override
    public String toString() {
        return date + ": " + closingPrice + " - " + volume;
    }
}

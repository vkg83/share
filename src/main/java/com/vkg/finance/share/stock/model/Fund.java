package com.vkg.finance.share.stock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Fund {
    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yy").toFormatter();

    private FundType type;
    private String symbol;
    private String assets;
    @JsonProperty("ltP")
    private double lastTradingPrice;
    @JsonProperty("qty")
    private long volume;
    @JsonProperty("xDt")
    private LocalDate actionDate;
    @JsonProperty("cAct")
    private String corporateAction;

    public void setType(FundType type) {
        this.type = type;
    }

    public FundType getType() {
        return type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getAssets() {
        return assets;
    }

    public void setAssets(String assets) {
        this.assets = assets;
    }

    public double getLastTradingPrice() {
        return lastTradingPrice;
    }

    public void setLastTradingPrice(double lastTradingPrice) {
        this.lastTradingPrice = lastTradingPrice;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public LocalDate getActionDate() {
        return actionDate;
    }

    public void setActionDate(String actionDate) {
        this.actionDate = "-".equals(actionDate)? null : LocalDate.parse(actionDate, FMT);
    }

    public String getCorporateAction() {
        return corporateAction;
    }

    public void setCorporateAction(String corporateAction) {
        this.corporateAction = corporateAction;
    }

}

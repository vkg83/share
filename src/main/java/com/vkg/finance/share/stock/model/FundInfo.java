package com.vkg.finance.share.stock.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundInfo {
    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yy").toFormatter();

    private FundType type;
    private String symbol;
    @JsonAlias("assets")
    private String name;
    private Double marketCap;
    @JsonAlias("xDt")
    private LocalDate actionDate;
    @JsonAlias("cAct")
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }
}

package com.vkg.finance.share.stock.repository.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceInfo {
    double open;
    double close;
    Map<String, Double> intraDayHighLow;

    public void setOpen(double open) {
        this.open = open;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setIntraDayHighLow(Map<String, Double> intraDayHighLow) {
        this.intraDayHighLow = intraDayHighLow;
    }
}

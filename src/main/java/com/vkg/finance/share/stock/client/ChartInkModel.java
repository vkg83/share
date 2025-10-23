package com.vkg.finance.share.stock.client;

import java.math.BigDecimal;

public class ChartInkModel {
    private final String symbol;
    private final BigDecimal price;
    private final long volume;

    public ChartInkModel(String symbol, String price, String volume) {
        this.symbol = symbol;
        this.price = new BigDecimal(price);
        this.volume = Long.parseLong(volume.replaceAll(",", ""));
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public long getVolume() {
        return volume;
    }
}

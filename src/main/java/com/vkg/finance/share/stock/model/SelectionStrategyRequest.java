package com.vkg.finance.share.stock.model;

import java.util.List;

public class SelectionStrategyRequest {
    private List<String> symbols;
    private List<Object> strategies;

    public List<String> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
    }

    public List<Object> getStrategies() {
        return strategies;
    }

    public void setStrategies(List<Object> strategies) {
        this.strategies = strategies;
    }
}

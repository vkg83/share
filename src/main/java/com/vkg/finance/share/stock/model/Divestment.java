package com.vkg.finance.share.stock.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

public class Divestment {
    public static final double STCG_TAX = 0.15;
    public static final double EDU_CESS = 1.04;
    private Investment investment;
    private double price;
    private LocalDate date;

    public Investment getInvestment() {
        return investment;
    }

    public void setInvestment(Investment investment) {
        this.investment = investment;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @JsonIgnore
    public double getGrossProfit() {
        return getSellAmount() - investment.getAmount();
    }

    @JsonIgnore
    public double getProfit() {
        return getGrossProfit() - brokerage() - tax();
    }

    @JsonIgnore
    public double getSellAmount() {
        return price * investment.getQuantity();
    }

    @JsonIgnore
    public double getBalance() {
        return investment.getAmount() + getProfit();
    }

    private double brokerage() {
        return (getSellAmount() + investment.getAmount()) * .0012 + 16;
    }

    private double tax() {
        return (getGrossProfit() - brokerage()) * STCG_TAX * EDU_CESS;
    }
}

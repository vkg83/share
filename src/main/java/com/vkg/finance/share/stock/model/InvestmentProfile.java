package com.vkg.finance.share.stock.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvestmentProfile {
    private final String profileName;
    private double balance;
    private List<Investment> investments = new ArrayList<>();
    private List<Divestment> divestments = new ArrayList<>();

    @JsonCreator
    public InvestmentProfile(@JsonProperty("profileName") String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return profileName;
    }

    public List<Investment> getInvestments() {
        return investments;
    }

    public void setInvestments(List<Investment> investments) {
        this.investments = investments;
    }

    public List<Divestment> getDivestments() {
        return divestments;
    }

    public void setDivestments(List<Divestment> divestments) {
        this.divestments = divestments;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void purchase(FundHistory history, double minAmount) {
        double price = history.getClosingPrice();
        int qty = (int) (minAmount / price) + 1;
        purchase(history.getSymbol(), history.getDate(), price, qty);
    }

    private void purchase(String symbol, LocalDate date, double price, int quantity) {
        if (balance < price * quantity) {
            throw new RuntimeException("Not enough balance!");
        }
        balance -= price * quantity;
        Investment inv = createInvestment(date, price, quantity, symbol);
        investments.add(inv);
    }


    private static Investment createInvestment(LocalDate date, double price, int quantity, String symbol) {
        Investment inv = new Investment();
        inv.setDate(date);
        inv.setStockSymbol(symbol);
        inv.setPrice(price);
        inv.setQuantity(quantity);
        return inv;
    }

    public void sellFirst(FundHistory history) {
        Investment investment = null;
        for (Investment value : investments) {
            if (value.getStockSymbol().equals(history.getSymbol())) {
                investment = value;
                break;
            }
        }
        if (investment == null) {
            throw new RuntimeException("No investment exists for " + history.getSymbol());
        }

        sell(investment, history);
    }

    public void sellLast(FundHistory history) {
        Investment investment = getLastInvestment(history.getSymbol());
        if (investment == null) {
            throw new RuntimeException("No investment exists for " + history.getSymbol());
        }

        sell(investment, history);
    }

    public Investment getLastInvestment(String symbol) {
        Investment investment = null;
        for (int i = investments.size() - 1; i >= 0; i--) {
            if (investments.get(i).getStockSymbol().equals(symbol)) {
                investment = investments.get(i);
                break;
            }
        }
        return investment;
    }

    public void sell(FundHistory history) {
        List<Investment> investmentList = new ArrayList<>();
        for (Investment investment : investments) {
            if (investment.getStockSymbol().equals(history.getSymbol())) {
                investmentList.add(investment);
            }
        }

        if (investmentList.isEmpty()) {
            throw new RuntimeException("No investment exists for " + history.getSymbol());
        }

        for (Investment investment : investmentList) {
            sell(investment, history);
        }
    }

    public void sell(Investment investment, FundHistory history) {
        investments.remove(investment);
        Divestment divestment = new Divestment();
        divestment.setInvestment(investment);
        divestment.setPrice(history.getClosingPrice());
        divestment.setDate(history.getDate());
        divestments.add(divestment);

        balance += divestment.getBalance();
    }

    public double getProfit() {
        return divestments.stream().mapToDouble(Divestment::getProfit).sum();
    }

    public double getGrossProfit() {
        return divestments.stream().mapToDouble(Divestment::getGrossProfit).sum();
    }

    public double getInvestedAmount() {
        return investments.stream()
                .mapToDouble(Investment::getAmount).sum();
    }

    public int getInvestedQuantity() {
        return investments.stream()
                .mapToInt(Investment::getQuantity).sum();
    }

    public double getInvestedAmount(String symbol) {
        return investments.stream().filter(inv -> inv.getStockSymbol().equals(symbol))
                .mapToDouble(Investment::getAmount).sum();
    }

    public int getInvestedQuantity(String symbol) {
        return investments.stream().filter(inv -> inv.getStockSymbol().equals(symbol))
                .mapToInt(Investment::getQuantity).sum();
    }

    public void updateSymbol(String symbol, String newSymbol) {
        investments.stream().filter(inv -> inv.getStockSymbol().equals(symbol))
                .forEach(i -> i.setStockSymbol(newSymbol));
    }
}

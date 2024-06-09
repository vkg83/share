package com.vkg.finance.share.stock.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vkg.finance.share.stock.service.SimpleInvestmentSimulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class InvestmentProfile {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleInvestmentSimulator.class);

    private final String profileName;
    private double balance;
    private double totalProfit;
    private List<Investment> investments = new ArrayList<>();
    private List<Investment> completedInvestments = new ArrayList<>();

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

    public List<Investment> getCompletedInvestments() {
        return completedInvestments;
    }

    public void setCompletedInvestments(List<Investment> completedInvestments) {
        this.completedInvestments = completedInvestments;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void purchase(FundHistory history, double minAmount) {
        double price = history.getClosingPrice();
        int qty = (int)(minAmount / price) + 1;
        purchase(history.getSymbol(), history.getDate(), price, qty);
        LOGGER.info("\tPurchase {} : {} * {}", history.getSymbol(), price, qty);
    }

    public void purchase(String symbol, LocalDate date, double price, int quantity) {
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

    public void sellFirst(FundInfo fundInfo, LocalDate date, double price) {
        Investment investment = null;
        for (Investment value : investments) {
            if (value.getStockSymbol().equals(fundInfo.getSymbol())) {
                investment = value;
                break;
            }
        }
        if (investment == null) {
            throw new RuntimeException("No investment exists for " + fundInfo.getSymbol());
        }

        sell(investment, date, price);
    }

    public void sellLast(FundInfo fundInfo, LocalDate date, double price) {
        Investment investment = null;
        for (int i = investments.size() - 1; i >= 0; i--) {
            if (investments.get(i).getStockSymbol().equals(fundInfo.getSymbol())) {
                investment = investments.get(i);
                break;
            }
        }
        if (investment == null) {
            throw new RuntimeException("No investment exists for " + fundInfo.getSymbol());
        }

        sell(investment, date, price);
    }

    private void sell(FundInfo fundInfo, LocalDate date, double price) {
        List<Investment> investmentList = new ArrayList<>();
        for (Investment investment : investments) {
            if (investment.getStockSymbol().equals(fundInfo.getSymbol())) {
                investmentList.add(investment);
            }
        }

        if (investmentList.isEmpty()) {
            throw new RuntimeException("No investment exists for " + fundInfo.getSymbol());
        }

        for (Investment investment : investmentList) {
            sell(investment, date, price);
        }
    }

    public void sell(Investment investment, FundHistory history) {
        sell(investment, history.getDate(), history.getClosingPrice());
    }

    public void sell(Investment inv, LocalDate date, double price) {
        investments.remove(inv);
        completedInvestments.add(inv);

        double curAmount = inv.getQuantity() * price;
        double previousAmount = inv.getQuantity() * inv.getPrice();

        double grossProfit = curAmount - previousAmount;
        double profit = grossProfit - brokerage(curAmount + previousAmount);
        profit -= tax(profit);
        totalProfit += profit;
        balance += previousAmount + profit;
        double percent = ((int)( (10000 * grossProfit) / (inv.getQuantity() * inv.getPrice())))/100.0;

        LOGGER.info("\tSale {} - {} : {}% - Period {}", inv.getStockSymbol(),((long)(profit * 100))/100.0, percent, Period.between(inv.getDate(), date));
    }

    private static double brokerage(double amount) {
        return amount * .0012 + 16;
    }

    private static double tax(double amount) {
        return amount * 0.15 * 1.04;
    }

    public boolean hasInvested(String symbol) {
        return investments.stream().anyMatch(i -> i.getStockSymbol().equals(symbol));
    }

    @Override
    public String toString() {
        return String.format("Balance: %8.2f, totalProfit: %7.2f, steps: %d, remaining %d", balance, totalProfit, completedInvestments.size(), investments.size());
    }
}

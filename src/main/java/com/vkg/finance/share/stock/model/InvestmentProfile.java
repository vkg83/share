package com.vkg.finance.share.stock.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class InvestmentProfile {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvestmentProfile.class);
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
        double price = history.getLastTradedPrice();
        int qty = (int) (minAmount / price) + 1;
        purchase(history.getSymbol(), history.getDate(), price, qty);
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

//        if (investmentList.isEmpty()) {
//            throw new RuntimeException("No investment exists for " + history.getSymbol());
//        }

        for (Investment investment : investmentList) {
            sell(investment, history);
        }
    }

    public void sell(Investment investment, FundHistory history) {
        investments.remove(investment);
        Divestment divestment = new Divestment();
        divestment.setInvestment(investment);
        divestment.setPrice(history.getLastTradedPrice());
        divestment.setDate(history.getDate());
        divestments.add(divestment);

        balance += divestment.getBalance();
    }

    @JsonIgnore
    public double getProfit() {
        return divestments.stream().mapToDouble(Divestment::getProfit).sum();
    }

    @JsonIgnore
    public double getGrossProfit() {
        return divestments.stream().mapToDouble(Divestment::getGrossProfit).sum();
    }

    @JsonIgnore
    public double getInvestedAmount() {
        return investments.stream()
                .mapToDouble(Investment::getAmount).sum();
    }

    @JsonIgnore
    public int getInvestedQuantity() {
        return investments.stream()
                .mapToInt(Investment::getQuantity).sum();
    }

    public double getInvestedAmount(String symbol) {
        return investments.stream().filter(inv -> inv.getStockSymbol().equals(symbol))
                .mapToDouble(Investment::getAmount).sum();
    }

    public int getInvestedCount(String symbol) {
        return (int)investments.stream().filter(inv -> inv.getStockSymbol().equals(symbol))
                .count();
    }

    public int getInvestedQuantity(String symbol) {
        return investments.stream().filter(inv -> inv.getStockSymbol().equals(symbol))
                .mapToInt(Investment::getQuantity).sum();
    }

    public void print() {
        Map<LocalDate, List<Investment>> holdMap = investments.stream().collect(Collectors.groupingBy(Investment::getDate));
        Map<LocalDate, List<Investment>> investmentMap = divestments.stream().map(Divestment::getInvestment).collect(Collectors.groupingBy(Investment::getDate));
        Map<LocalDate, List<Divestment>> divestmentMap = divestments.stream().collect(Collectors.groupingBy(Divestment::getDate));
        Set<LocalDate> dates = new TreeSet<>(holdMap.keySet());
        dates.addAll(investmentMap.keySet());
        dates.addAll(divestmentMap.keySet());
        LocalDate today = LocalDate.now();
        for (LocalDate date : dates) {
            LOGGER.info("Date: {}", date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy (EEE)")));
            var bookings = divestmentMap.getOrDefault(date, List.of());
            bookings.forEach(b -> {
                var i = b.getInvestment();
                var percent = b.getGrossProfit() * 100 / i.getAmount();
                var s = String.format("%1.2f, %1.2f%%", b.getProfit(), percent);
                LOGGER.info("\tSold {} {} {} {}", i.getQuantity(), i.getStockSymbol(), s, Period.between(i.getDate(), date));
            });
            var investments = holdMap.getOrDefault(date, List.of());
            investments.forEach(i ->
                    LOGGER.info("\tHold {} {}({} * {}): {}", Period.between(i.getDate(), today), i.getStockSymbol(), i.getQuantity(), i.getPrice(), ((int)(100*i.getAmount()))/100.0)
            );
            investments = investmentMap.getOrDefault(date, List.of());
            investments.forEach(i ->
                    LOGGER.info("\tPurchased {}({}): {}", i.getStockSymbol(), i.getPrice(), ((int)(100*i.getAmount()))/100.0)
            );
        }

        var s = String.format("Balance: %8.2f, invested: %8.2f, grossProfit: %1.2f totalProfit: %7.2f, steps: %d, remaining %d", getBalance(), getInvestedAmount(), getGrossProfit(), getProfit(), divestments.size(), investments.size());
        LOGGER.info("Final {}", s);
    }
}

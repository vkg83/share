package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.client.FundDataProvider;
import com.vkg.finance.share.stock.model.*;
import com.vkg.finance.share.stock.strategies.MovingAverageStrategy;
import com.vkg.finance.share.stock.strategies.SimpleFundSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimpleInvestmentSimulator implements InvestmentSimulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleInvestmentSimulator.class);
    public static final int DAILY_FUND = 3333;
    @Autowired
    private FundDataProvider dataProvider;

    @Override
    public void simulateLifoShop() {

    }

    @Override
    public void simulateEtfShop() {
        InvestmentProfile p = new InvestmentProfile("sim_etf");
        p.setBalance(400000);

        List<FundInfo> allFundInfos = dataProvider.getAllFunds(FundType.ETF);


        var s = new SimpleFundSelector(dataProvider)
                .setCurrentDate(LocalDate.now())
                .setMinVolume(5000).excludeAssets("GOLD", "SILVER", "LIQUID");
        List<FundInfo>etfs = s.select(allFundInfos);
        List<FundInfo> jwel = new SimpleFundSelector(dataProvider)
                .setCurrentDate(LocalDate.now())
                .setMinVolume(5000).includeAssets("GOLD", "SILVER").select(allFundInfos);
        List<FundInfo> stocks = load("HDFCBANK", "RELIANCE", "ICICIBANK", "INFY", "ITC", "TCS", "AXISBANK", "LT", "KOTAKBANK", "HINDUNILVR");

        MovingAverageStrategy strategy = new MovingAverageStrategy(dataProvider);
        LocalDate today = LocalDate.now();
        LocalDate curDate = today.minusYears(1);
        while(curDate.isBefore(today)) {
            curDate = curDate.plusDays(1);
            if(curDate.getDayOfWeek() == DayOfWeek.SATURDAY || curDate.getDayOfWeek() == DayOfWeek.SUNDAY)
                continue;
            LOGGER.info("{}: {}", curDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy (EEE)")), p);
            strategy.setCurrentDate(curDate);

            boolean purchased1 = process(p, curDate, strategy.select(etfs));
            boolean purchased2 = process(p, curDate, strategy.select(jwel));
            boolean purchased3 = process(p, curDate, strategy.select(stocks));
//            if(!(purchased1 || purchased2 || purchased3))
//                tryAverage(p, curDate);
        }
        LOGGER.info("Final {}", p);
    }

    private static List<FundInfo> load(String... symbols) {
        return Arrays.stream(symbols).map(SimpleInvestmentSimulator::toInfo).collect(Collectors.toList());
    }

    private static FundInfo toInfo(String symbol) {
        FundInfo info = new FundInfo();
        info.setSymbol(symbol);
        info.setName(symbol);
        return info;
    }

    private boolean process(InvestmentProfile p, LocalDate curDate, List<FundInfo> fundInfos) {
        trySell(p, curDate);
        return tryPurchase(p, fundInfos.subList(0, Math.min(fundInfos.size(), 3)), curDate, DAILY_FUND);
    }

    private void tryAverage(InvestmentProfile p, LocalDate curDate) {
        final Set<Investment> funds = new HashSet<>(p.getInvestments());
        for (Investment fund : funds) {
            if (avgNeeded(p, curDate, fund.getStockSymbol())) {
                purchase(p, fund.getStockSymbol(), curDate, DAILY_FUND);
                return;
            }
        }
    }

    private void trySell(InvestmentProfile p, LocalDate curDate) {
        double maxProfit = 0;

        Investment inv = null;
        FundHistory his = null;
        for (Investment i : p.getInvestments()) {
            final Optional<FundHistory> history = dataProvider.getHistory(i.getStockSymbol(), curDate);
            if (history.isEmpty()) {
                //LOGGER.warn("No current price available for {} on {} for sale", i.getStockSymbol(), curDate);
                continue;
            }
            FundHistory h = history.get();
            double price = h.getClosingPrice();


            if (i.getPrice() * 1.03 < price) {
                double profit = (price - i.getPrice()) * i.getQuantity();
                if (maxProfit < profit) {
                    maxProfit = profit;
                    inv = i;
                    his = h;
                }
            }
        }
        if(inv != null) {
            p.sell(inv, his);
        }
    }

    private boolean tryPurchase(InvestmentProfile p, List<FundInfo> fundInfos, LocalDate date, double amount) {
        if(fundInfos.isEmpty()) return false;
        final String symbol = fundInfos.get(0).getSymbol();
        if(p.hasInvested(symbol)) {
            return tryPurchase(p, fundInfos.subList(1, fundInfos.size()), date, amount);
        } else {
            return purchase(p, symbol, date, amount);
        }
    }

    private boolean purchase(InvestmentProfile p, String symbol, LocalDate date, double amount) {
        final Optional<FundHistory> history = dataProvider.getHistory(symbol, date);
        if(history.isEmpty()) {
            //LOGGER.warn("No current price available for {} on {} for purchase", symbol.getSymbol(), date);
            return false;
        }

        FundHistory h = history.get();
        try {
            p.purchase(h, amount);
        } catch(Exception e) {
            LOGGER.warn("Skipping! {}", e.getMessage());
            return false;
        }

        return true;
    }

    private boolean avgNeeded(InvestmentProfile p, LocalDate date, String symbol) {
        final List<Investment> invs = p.getInvestments().stream().filter(i -> i.getStockSymbol().equals(symbol)).collect(Collectors.toList());
        if(!invs.isEmpty()) {
            final Optional<FundHistory> history = dataProvider.getHistory(symbol, date);
            if(history.isEmpty()) return false;
            double price = history.get().getClosingPrice();
            double a = invs.stream().mapToDouble(i ->i.getPrice() * i.getQuantity()).sum();
            int b = invs.stream().mapToInt(Investment::getQuantity).sum();
            double avgPrice = a / b;

            if(price < avgPrice * (1 - invs.size() * 0.05)) {
                LOGGER.info("\tAveraging needed for {} lots of {}", invs.size(), symbol);
                return true;
            } else
                return false;
        }
        return false;
    }

}

package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.client.FundDataProvider;
import com.vkg.finance.share.stock.config.MarketConfig;
import com.vkg.finance.share.stock.model.*;
import com.vkg.finance.share.stock.strategies.MovingAverageStrategy;
import com.vkg.finance.share.stock.strategies.SimpleFundSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@EnableConfigurationProperties(MarketConfig.class)
public class SimpleInvestmentSimulator implements InvestmentSimulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleInvestmentSimulator.class);
    public static final int DAILY_FUND = 3333;
    @Autowired
    private FundDataProvider dataProvider;

    @Autowired
    private MarketConfig marketConfig;

    public void doLifoShop(InvestmentProfile p) {
        List<FundInfo> allFundInfos = dataProvider.getAllFunds(FundType.ETF);

        LocalDate today = LocalDate.now().minusDays(1);
        var s = new SimpleFundSelector(dataProvider)
                .setCurrentDate(today)
                .setMinVolume(5000).excludeAssets("GOLD", "SILVER", "LIQUID");
        List<FundInfo> etfs = s.select(allFundInfos);
        List<FundInfo> jwel = new SimpleFundSelector(dataProvider)
                .setCurrentDate(today)
                .setMinVolume(5000).includeAssets("GOLD", "SILVER").select(allFundInfos);
        List<FundInfo> stocks = load("HDFCBANK", "RELIANCE", "ICICIBANK", "INFY", "ITC", "TCS", "AXISBANK", "LT", "KOTAKBANK", "HINDUNILVR");

        MovingAverageStrategy strategy = new MovingAverageStrategy(dataProvider);

        if (marketConfig.isMarketClosed(today)) {
            LOGGER.info("Market closed on {}", today);
            return;
        }

        strategy.setCurrentDate(today);
        double dailyFund = DAILY_FUND * (1 + (p.getDivestments().size() * 0.0375)/120);

        boolean purchased1 = processLast(p, today, strategy.select(etfs), dailyFund);
        boolean purchased2 = processLast(p, today, strategy.select(jwel), dailyFund);
        boolean purchased3 = processLast(p, today, strategy.select(stocks), dailyFund);

        if (!(purchased1 || purchased2 || purchased3))
            tryAverage(p, today, dailyFund);

        print(p);
    }

    @Override
    public void simulateLifoShop() {
        InvestmentProfile p = new InvestmentProfile("sim_etf");
        p.setBalance(400000);

        List<FundInfo> allFundInfos = dataProvider.getAllFunds(FundType.ETF);

        LocalDate today = LocalDate.now();
        var s = new SimpleFundSelector(dataProvider)
                .setCurrentDate(today)
                .setMinVolume(5000).excludeAssets("GOLD", "SILVER", "LIQUID");
        List<FundInfo> etfs = s.select(allFundInfos);
        List<FundInfo> jwel = new SimpleFundSelector(dataProvider)
                .setCurrentDate(today)
                .setMinVolume(5000).includeAssets("GOLD", "SILVER").select(allFundInfos);
        List<FundInfo> stocks = load("HDFCBANK", "RELIANCE", "ICICIBANK", "INFY", "ITC", "TCS", "AXISBANK", "LT", "KOTAKBANK", "HINDUNILVR");

        MovingAverageStrategy strategy = new MovingAverageStrategy(dataProvider);

        double dailyFund = DAILY_FUND;
        LocalDate curDate = today.minusYears(1);
        while (curDate.isBefore(today)) {
            curDate = curDate.plusDays(1);
            if (marketConfig.isMarketClosed(curDate)) {
                continue;
            }
            strategy.setCurrentDate(curDate);

            boolean purchased1 = processLast(p, curDate, strategy.select(etfs), dailyFund);
            boolean purchased2 = processLast(p, curDate, strategy.select(jwel), dailyFund);
            boolean purchased3 = processLast(p, curDate, strategy.select(stocks), dailyFund);
            if (!(purchased1 || purchased2 || purchased3))
                tryAverage(p, curDate, dailyFund);

            dailyFund = DAILY_FUND * (1 + (p.getDivestments().size() * 0.0375)/120);
        }

        print(p);
    }

    @Override
    public void simulateEtfShop() {
        InvestmentProfile p = new InvestmentProfile("sim_etf");
        p.setBalance(400000);

        List<FundInfo> allFundInfos = dataProvider.getAllFunds(FundType.ETF);

        LocalDate today = LocalDate.now();
        var s = new SimpleFundSelector(dataProvider)
                .setCurrentDate(today)
                .setMinVolume(5000).excludeAssets("GOLD", "SILVER", "LIQUID");
        List<FundInfo> etfs = s.select(allFundInfos);
        List<FundInfo> jwel = new SimpleFundSelector(dataProvider)
                .setCurrentDate(today)
                .setMinVolume(5000).includeAssets("GOLD", "SILVER").select(allFundInfos);
        List<FundInfo> stocks = load("HDFCBANK", "RELIANCE", "ICICIBANK", "INFY", "ITC", "TCS", "AXISBANK", "LT", "KOTAKBANK", "HINDUNILVR");

        MovingAverageStrategy strategy = new MovingAverageStrategy(dataProvider);

        final int dailyFund = DAILY_FUND;
        LocalDate curDate = today.minusYears(1);
        while (curDate.isBefore(today)) {
            curDate = curDate.plusDays(1);
            if (marketConfig.isMarketClosed(curDate)) {
                continue;
            }
            strategy.setCurrentDate(curDate);

            boolean purchased1 = process(p, curDate, strategy.select(etfs), dailyFund);
            boolean purchased2 = process(p, curDate, strategy.select(jwel), dailyFund);
            boolean purchased3 = process(p, curDate, strategy.select(stocks), dailyFund);
            if (!(purchased1 || purchased2 || purchased3))
                tryAverage(p, curDate, dailyFund);
        }

        print(p);
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

    private boolean process(InvestmentProfile p, LocalDate curDate, List<FundInfo> fundInfos, double dailyFund) {
        trySell(p, curDate);
        return tryPurchase(p, fundInfos.subList(0, Math.min(fundInfos.size(), 5)), curDate, dailyFund);
    }

    private boolean processLast(InvestmentProfile p, LocalDate curDate, List<FundInfo> fundInfoList, double dailyFund) {
        trySellLast(p, curDate);
        return tryPurchase(p, fundInfoList.subList(0, Math.min(fundInfoList.size(), 5)), curDate, dailyFund);
    }


    private void trySellLast(InvestmentProfile p, LocalDate curDate) {
        double maxProfit = 0;

        FundHistory his = null;
        final Set<String> symbols = p.getInvestments().stream().map(Investment::getStockSymbol).collect(Collectors.toSet());
        for(String symbol : symbols) {
            final Optional<FundHistory> history = dataProvider.getHistory(symbol, curDate);
            if (history.isEmpty()) {
                //LOGGER.warn("No current price available for {} on {} for sale", i.getStockSymbol(), curDate);
                continue;
            }
            FundHistory h = history.get();
            double price = h.getClosingPrice();
            if(!h.getSymbol().equals(symbol)) {
                LOGGER.info("Symbol name {} changed to {} As on {}", symbol, h.getSymbol(), curDate);
                p.updateSymbol(symbol, h.getSymbol());
            }
            Investment inv = p.getLastInvestment(h.getSymbol());
            double am = inv.getAmount();
            int qty = p.getInvestedQuantity(h.getSymbol());
            double target = .05;
            if(qty == 4) {
                target *= 2;
            } else if(qty > 4) {
                target *= 3;
            }
            target += 1;
            if(inv.getPrice() * target < price) {
                double profit = price * qty - am;
                if(maxProfit < profit) {
                    maxProfit = profit;
                    his = h;
                }
            }
        }

        if(his != null) {
            p.sellLast(his);
        }
    }


    private void trySell(InvestmentProfile p, LocalDate curDate) {
        double maxProfit = 0;

        FundHistory his = null;
        final Set<String> symbols = p.getInvestments().stream().map(Investment::getStockSymbol).collect(Collectors.toSet());
        for(String symbol : symbols) {
            final Optional<FundHistory> history = dataProvider.getHistory(symbol, curDate);
            if (history.isEmpty()) {
                //LOGGER.warn("No current price available for {} on {} for sale", i.getStockSymbol(), curDate);
                continue;
            }
            FundHistory h = history.get();
            double price = h.getClosingPrice();
            if(!h.getSymbol().equals(symbol)) {
                LOGGER.info("Symbol name {} changed to {} As on {}", symbol, h.getSymbol(), curDate);
                p.updateSymbol(symbol, h.getSymbol());
            }
            double am = p.getInvestedAmount(symbol);
            int qty = p.getInvestedQuantity(symbol);
            if(am / qty * 1.05 < price) {
                double profit = price * qty - am;
                if(maxProfit < profit) {
                    maxProfit = profit;
                    his = h;
                }
            }
        }

        if(his != null) {
            p.sell(his);
        }
    }

    private boolean tryPurchase(InvestmentProfile p, List<FundInfo> fundInfos, LocalDate date, double amount) {
        if (fundInfos.isEmpty()) return false;
        final String symbol = fundInfos.get(0).getSymbol();
        if (p.getInvestedQuantity(symbol) > 0) {
            return tryPurchase(p, fundInfos.subList(1, fundInfos.size()), date, amount);
        } else {
            return purchase(p, symbol, date, amount);
        }
    }

    private boolean purchase(InvestmentProfile p, String symbol, LocalDate date, double amount) {
        final Optional<FundHistory> history = dataProvider.getHistory(symbol, date);
        if (history.isEmpty()) {
            //LOGGER.warn("No current price available for {} on {} for purchase", symbol.getSymbol(), date);
            return false;
        }

        FundHistory h = history.get();
        try {
            p.purchase(h, amount);
        } catch (Exception e) {
            LOGGER.warn("Skipping! {}", e.getMessage());
            return false;
        }

        return true;
    }

    private void tryAverage(InvestmentProfile p, LocalDate curDate, double dailyFund) {
        final Set<String> symbols = p.getInvestments().stream().map(Investment::getStockSymbol).collect(Collectors.toSet());
        for (String symbol : symbols) {
            if (avgNeeded(p, curDate, symbol)) {
                purchase(p, symbol, curDate, dailyFund);
                return;
            }
        }
    }

    private boolean avgNeeded(InvestmentProfile p, LocalDate date, String symbol) {
        final Optional<FundHistory> history = dataProvider.getHistory(symbol, date);
        if(history.isEmpty()) {
            return false;
        }
        FundHistory h = history.get();
        if(!h.getSymbol().equals(symbol)) {
            LOGGER.info("Symbol name {} changed to {} As on {}", symbol, h.getSymbol(), date);
            p.updateSymbol(symbol, h.getSymbol());
        }

        final double totalCost = p.getInvestedAmount(symbol);
        final double qty = p.getInvestedQuantity(symbol);

        if (qty <= 0) {
            return false;
        }

        return h.getClosingPrice() < totalCost / qty * (1 - qty * 0.05);
    }

    private void print(InvestmentProfile p) {
        Map<LocalDate, List<Investment>> holdMap = p.getInvestments().stream().collect(Collectors.groupingBy(Investment::getDate));
        Map<LocalDate, List<Investment>> investmentMap = p.getDivestments().stream().map(Divestment::getInvestment).collect(Collectors.groupingBy(Investment::getDate));
        Map<LocalDate, List<Divestment>> divestmentMap = p.getDivestments().stream().collect(Collectors.groupingBy(Divestment::getDate));
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
                LOGGER.info("\tSold {} {} {}",i.getStockSymbol(), s, Period.between(i.getDate(), date));
            });
            var investments = holdMap.getOrDefault(date, List.of());
            investments.forEach(i ->
                LOGGER.info("\tHold {} {}({}): {}", Period.between(i.getDate(), today), i.getStockSymbol(), i.getPrice(), ((int)(100*i.getAmount()))/100.0)
            );
            investments = investmentMap.getOrDefault(date, List.of());
            investments.forEach(i ->
                LOGGER.info("\tPurchased {}({}): {}", i.getStockSymbol(), i.getPrice(), ((int)(100*i.getAmount()))/100.0)
            );
        }

        var s = String.format("Balance: %8.2f, invested: %8.2f, grossProfit: %1.2f totalProfit: %7.2f, steps: %d, remaining %d", p.getBalance(), p.getInvestedAmount(), p.getGrossProfit(), p.getProfit(), p.getDivestments().size(), p.getInvestments().size());
        LOGGER.info("Final {}", s);
    }

}

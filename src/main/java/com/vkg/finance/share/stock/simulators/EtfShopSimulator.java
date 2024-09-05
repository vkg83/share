package com.vkg.finance.share.stock.simulators;

import com.vkg.finance.share.stock.config.MarketConfig;
import com.vkg.finance.share.stock.model.*;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import com.vkg.finance.share.stock.service.FundManagementService;
import com.vkg.finance.share.stock.strategies.MovingAverageStrategy;
import com.vkg.finance.share.stock.strategies.SimpleFundSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@EnableConfigurationProperties(MarketConfig.class)
public class EtfShopSimulator implements InvestmentSimulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(EtfShopSimulator.class);
    public static final int DAILY_FUND = 5000;
    public static final int MIN_VOLUME = 10000;
    public static final int TARGET_PERCENT = 10;
    public static final int MAX_SELECTION = 5;
    @Autowired
    private MarketDataProvider dataProvider;
    @Autowired
    private FundManagementService fundManagementService;

    @Autowired
    private MarketConfig marketConfig;

    public void simulateForDay(InvestmentProfile p, LocalDate today) {

        if (marketConfig.isMarketClosed(today)) {
            LOGGER.info("Market closed on {}", today);
            return;
        }

        List<FundInfo> allFundInfos = dataProvider.getAllFunds(FundType.ETF);
        List<FundInfo> etfs = getEtfs(allFundInfos, today);
        List<FundInfo> jwel = getJewellery(allFundInfos, today);
        List<FundInfo> stocks = getStocks();

        MovingAverageStrategy strategy = new MovingAverageStrategy(dataProvider);

        double dailyFund = DAILY_FUND * (1 + (p.getDivestments().size() * 0.0375)/120);

        boolean purchased1 = process(p, today, strategy.select(etfs, today), dailyFund);
        boolean purchased2 = process(p, today, strategy.select(jwel, today), dailyFund);
        boolean purchased3 = process(p, today, strategy.select(stocks, today), dailyFund);

        if (!(purchased1 || purchased2 || purchased3)) {
            LOGGER.info("Not purchased anything. So trying average");
            tryAverage(p, today, dailyFund);
        }

        print(p);
    }
    @Override
    public void simulate() {
        InvestmentProfile p = new InvestmentProfile("sim_etf");
        p.setBalance(400000);

        List<FundInfo> allFundInfos = dataProvider.getAllFunds(FundType.ETF);

        LocalDate today = LocalDate.now();
        List<FundInfo> etfs = getEtfs(allFundInfos, today);
        List<FundInfo> jwel = getJewellery(allFundInfos, today);
        List<FundInfo> stocks = getStocks();

        MovingAverageStrategy strategy = new MovingAverageStrategy(dataProvider);

        double dailyFund = DAILY_FUND;
        LocalDate curDate = today.minusYears(1);
        while (curDate.isBefore(today)) {
            curDate = curDate.plusDays(1);
            if (marketConfig.isMarketClosed(curDate)) {
                continue;
            }

            boolean purchased1 = process(p, curDate, strategy.select(etfs, curDate), dailyFund);
            boolean purchased2 = process(p, curDate, strategy.select(jwel, curDate), dailyFund);
            boolean purchased3 = process(p, curDate, strategy.select(stocks, curDate), dailyFund);
            if (!(purchased1 || purchased2 || purchased3))
                tryAverage(p, curDate, dailyFund);

            dailyFund = DAILY_FUND * (1 + (p.getDivestments().size() * 0.0375)/120);
        }

        print(p);
    }

    private List<FundInfo> getEtfs(List<FundInfo> allFundInfos, LocalDate today) {
        return new SimpleFundSelector(dataProvider)
                .setMinVolume(MIN_VOLUME)
                .excludeAssets("GOLD", "SILVER", "LIQUID", "GSEC", "GILT", "BOND", "SDL")
                .select(allFundInfos, today);
    }

    private List<FundInfo> getJewellery(List<FundInfo> allFundInfos, LocalDate today) {
        return new SimpleFundSelector(dataProvider)
                .setMinVolume(MIN_VOLUME).includeAssets("GOLD", "SILVER").select(allFundInfos, today);
    }

    private List<FundInfo> getStocks() {
        return fundManagementService.getFundDetails().stream()
                .filter(i -> i.getMarketCap()!=null)
                .sorted(Comparator.comparing(FundInfo::getMarketCap).reversed()).limit(25).collect(Collectors.toList());
    }

    private boolean process(InvestmentProfile p, LocalDate curDate, List<FundInfo> fundInfos, double dailyFund) {
        trySell(p, curDate);
        return tryPurchase(p, fundInfos.subList(0, Math.min(fundInfos.size(), MAX_SELECTION)), curDate, dailyFund);
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
            double price = h.getLastTradedPrice();
            double am = p.getInvestedAmount(symbol);
            int qty = p.getInvestedQuantity(symbol);
            if(am / qty * (1 + TARGET_PERCENT / 100.0) < price) {
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
            LOGGER.warn("Skipping purchase on {}! {}", date, e.getMessage());
            return false;
        }

        return true;
    }

    private void tryAverage(InvestmentProfile p, LocalDate curDate, double dailyFund) {
        final Set<String> symbols = p.getInvestments().stream().map(Investment::getStockSymbol).collect(Collectors.toSet());
        for (String symbol : symbols) {
            if (avgNeeded(p, curDate, symbol)) {
                LOGGER.info("AVG needed in {}", symbol);
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
        }

        final double totalCost = p.getInvestedAmount(symbol);
        final double qty = p.getInvestedQuantity(symbol);
        final int count = p.getInvestedCount(symbol);
        final double avgPrice = totalCost / qty;

        if (qty <= 0) {
            return false;
        }

        return h.getLastTradedPrice() < avgPrice * (1 - count * 0.05);
    }

    private void print(InvestmentProfile p) {
        p.print();
        LOGGER.info("Remaining: {}", p.getInvestments().stream().map(this::symbolWithProfit)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> String.format("%s %6.2f %%", e.getKey(),e.getValue()))
                .collect(Collectors.joining(" | ")));
    }

    private Map.Entry<String, Double> symbolWithProfit(Investment investment) {
        double p = dataProvider.getHistory(investment.getStockSymbol(), LocalDate.now()).map(FundHistory::getLastTradedPrice).orElse(0.0);
        return new AbstractMap.SimpleImmutableEntry<>(investment.getStockSymbol(), (p - investment.getPrice()) * 100/ investment.getPrice());
    }
}

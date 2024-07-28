package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.config.MarketConfig;
import com.vkg.finance.share.stock.model.*;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import com.vkg.finance.share.stock.strategies.*;
import com.vkg.finance.share.stock.trade.PurchaseFresh;
import com.vkg.finance.share.stock.trade.SellLastOnPercent;
import com.vkg.finance.share.stock.trade.TradeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@EnableConfigurationProperties(MarketConfig.class)
public class SimpleInvestmentSimulator implements InvestmentSimulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleInvestmentSimulator.class);
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

    public void doLifoShop(InvestmentProfile p, LocalDate today) {

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

        boolean purchased1 = processLast(p, today, strategy.select(etfs, today), dailyFund);
        boolean purchased2 = processLast(p, today, strategy.select(jwel, today), dailyFund);
        boolean purchased3 = processLast(p, today, strategy.select(stocks, today), dailyFund);

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
        List<FundInfo> etfs = getEtfs(allFundInfos, today);
        List<FundInfo> jwel = getJewellery(allFundInfos, today);
        List<FundInfo> stocks = getStocks();

        MovingAverageStrategy strategy = new MovingAverageStrategy(dataProvider);

        double dailyFund = DAILY_FUND;
        LocalDate curDate = today.minusYears(1).minusDays(30);
        StringBuilder balStr = new StringBuilder();
        while (curDate.isBefore(today)) {
            curDate = curDate.plusDays(1);
            if (marketConfig.isMarketClosed(curDate)) {
                continue;
            }

            boolean purchased1 = processLast(p, curDate, strategy.select(etfs, curDate), dailyFund);
            boolean purchased2 = processLast(p, curDate, strategy.select(jwel, curDate), dailyFund);
            boolean purchased3 = processLast(p, curDate, strategy.select(stocks, curDate), dailyFund);
            if (!(purchased1 || purchased2 || purchased3))
                tryAverage(p, curDate, dailyFund);

            dailyFund = DAILY_FUND * (1 + (p.getDivestments().size() * 0.0375)/120);

            balStr.append(" | ").append(((long) p.getBalance() * 100) / 100.0);
        }

        print(p);
        LOGGER.info("New Daily Fund: {}", dailyFund);

        System.out.println(balStr);
    }

    @Override
    public void simulateEtfShop() {
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
                .excludeAssets("GOLD", "SILVER", "LIQUID", "GSEC", "GILT")
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

    private boolean processLast(InvestmentProfile p, LocalDate curDate, List<FundInfo> fundInfoList, double dailyFund) {
        trySellLast(p, curDate);
        return tryPurchase(p, fundInfoList.subList(0, Math.min(fundInfoList.size(), MAX_SELECTION)), curDate, dailyFund);
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
            double price = h.getLastTradedPrice();
            Investment inv = p.getLastInvestment(h.getSymbol());
            double am = inv.getAmount();
            int count = p.getInvestedCount(h.getSymbol());
            int qty = p.getInvestedQuantity(h.getSymbol());
            double target = TARGET_PERCENT / 100.0;
            if(count == 4) {
                target *= 2;
            } else if(count > 4) {
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

        if (qty <= 0) {
            return false;
        }

        return h.getLastTradedPrice() < totalCost / qty * (1 - qty * 0.05);
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

    public void simulateDarvos(InvestmentProfile p, FundInfo stock) {
        DarvosTradingStrategy strategy = new DarvosTradingStrategy(dataProvider);
        LocalDate today = LocalDate.now();
        LocalDate currentDate = today.minusYears(1);
        while(currentDate.isBefore(today)) {
            strategy.setCurrentDate(currentDate);
            var flag = strategy.buy(stock);
            if(flag != null) {
                LOGGER.info("Buy on {} for {}", currentDate, flag.getLastTradedPrice());
                currentDate = currentDate.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
                p.purchase(flag, 5000);
            }
            var cur = dataProvider.getHistory(stock.getSymbol(), currentDate);
            cur.ifPresent(history -> calcNotionalProfit(p, history));
            currentDate = currentDate.plusDays(1);
        }
        LOGGER.info("Notional profit {}, {}%", p.getProfit(), p.getGrossProfit() * 100 / p.getInvestedAmount());
    }

    private void calcNotionalProfit(InvestmentProfile p, FundHistory h) {
        double profit = 0;
        List<Investment> investments = new ArrayList<>();
        for (var i: p.getInvestments()) {
            if(i.getAmount() * 1.05 < h.getLastTradedPrice() * i.getQuantity()) {
                profit  += h.getLastTradedPrice() * i.getQuantity() - i.getAmount();
                investments.add(i);
            }
        }

        for(var i: investments) {
            p.sell(i, h);
        }

        if(profit > 0) {
            LOGGER.info("Notional profit {}, {}%", profit, (int )(profit * 100 / p.getInvestedAmount()));
        }
    }

    public void simulate() {
        List<FundInfo> stocks = getETFs();
        Simulation simulation = new Simulation(stocks, marketConfig);

        simulation.setPurchaseStrategy(this::preparePurchaseModel);
        simulation.setSellStrategy(this::prepareSellModel);
        final LocalDate today = LocalDate.now();
        var p = simulation.simulate(500000, today.minusYears(1), today);
        p.print();
    }

    private TradeModel prepareSellModel(InvestmentProfile investmentProfile) {
        SellLastOnPercent model2 = new SellLastOnPercent(investmentProfile, dataProvider, 5);
        AbstractSelectionStrategy strategy2 = new MASaleStrategy(dataProvider);
        model2.setStrategy(strategy2);
        return model2;
    }

    private TradeModel preparePurchaseModel(InvestmentProfile investmentProfile) {
        PurchaseFresh model1 = new PurchaseFresh(investmentProfile, dataProvider);
        MAStrategy strategy1 = new MAStrategy(dataProvider);
        final LimitedSelection limit = new LimitedSelection(10);
        model1.setStrategy(strategy1.setNext(limit));
        return model1;
    }

    private List<FundInfo> getETFs() {
        Predicate<FundInfo> filter = i1 -> true;
        for (String asset : List.of("GOLD", "SILVER", "LIQUID", "GSEC", "GILT")) {
            Predicate<FundInfo> e = f->f.getName().toUpperCase().contains(asset);
            e = e.or(f->f.getSymbol().contains(asset));
            filter = filter.and(e.negate());
        }
        return dataProvider.getAllFunds(FundType.ETF).stream().filter(filter).collect(Collectors.toList());
    }

}

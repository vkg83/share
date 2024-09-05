package com.vkg.finance.share.stock.simulators;

import com.vkg.finance.share.stock.config.MarketConfig;
import com.vkg.finance.share.stock.model.*;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import com.vkg.finance.share.stock.service.Simulation;
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
import java.util.ArrayList;
import java.util.List;
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
    private MarketConfig marketConfig;


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

    @Override
    public void simulate() {
        List<FundInfo> stocks = getETFs();
        Simulation simulation = new Simulation(stocks, marketConfig);

        simulation.setPurchaseStrategy(this::preparePurchaseModel);
        simulation.setSellStrategy(this::prepareSellModel);
        final LocalDate today = LocalDate.now();
        var p = simulation.simulate(500000, today.minusYears(5), today);
        p.print();
    }

    @Override
    public void simulateForDay(InvestmentProfile profile, LocalDate date) {
        List<FundInfo> stocks = getETFs();
        Simulation simulation = new Simulation(stocks, marketConfig);

        simulation.setPurchaseStrategy(this::preparePurchaseModel);
        simulation.setSellStrategy(this::prepareSellModel);
        simulation.simulate(profile, date);
        profile.print();
    }

    private TradeModel prepareSellModel(InvestmentProfile investmentProfile) {
        SellLastOnPercent model = new SellLastOnPercent(investmentProfile, dataProvider, TARGET_PERCENT);
        AbstractSelectionStrategy strategy = new MASaleStrategy(dataProvider);
        model.setStrategy(strategy);
        return model;
    }

    private TradeModel preparePurchaseModel(InvestmentProfile investmentProfile) {
        PurchaseFresh model = new PurchaseFresh(investmentProfile, dataProvider);
        MAPurchaseStrategy strategy = new MAPurchaseStrategy(dataProvider);
        final LimitedSelection limit = new LimitedSelection(MAX_SELECTION);
        model.setStrategy(strategy.setNext(limit));
        return model;
    }

    private List<FundInfo> getETFs() {
        Predicate<FundInfo> filter = i1 -> true;
        for (String asset : List.of("GOLD", "SILVER", "LIQUID", "GSEC", "GILT", "BOND", "SDL")) {
            Predicate<FundInfo> e = f->f.getName().toUpperCase().contains(asset);
            e = e.or(f->f.getSymbol().contains(asset));
            filter = filter.and(e.negate());
        }
        return dataProvider.getAllFunds(FundType.ETF).stream().filter(filter).collect(Collectors.toList());
    }

}

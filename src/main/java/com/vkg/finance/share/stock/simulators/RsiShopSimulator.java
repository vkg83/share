package com.vkg.finance.share.stock.simulators;

import com.vkg.finance.share.stock.config.MarketConfig;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundType;
import com.vkg.finance.share.stock.model.FundWrapper;
import com.vkg.finance.share.stock.model.InvestmentProfile;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import com.vkg.finance.share.stock.service.FundManagementService;
import com.vkg.finance.share.stock.service.Simulation;
import com.vkg.finance.share.stock.strategies.AbstractSelectionStrategy;
import com.vkg.finance.share.stock.strategies.GenericSelectionStrategy;
import com.vkg.finance.share.stock.strategies.LimitedSelection;
import com.vkg.finance.share.stock.trade.PurchaseFresh;
import com.vkg.finance.share.stock.trade.SellLastOnPercent;
import com.vkg.finance.share.stock.trade.TradeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@EnableConfigurationProperties(MarketConfig.class)
public class RsiShopSimulator implements InvestmentSimulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RsiShopSimulator.class);
    public static final int DAILY_FUND = 5000;
    public static final int MIN_VOLUME = 10000;
    public static final double TARGET_PERCENT = 6.28;
    public static final int MAX_SELECTION = 5;
    @Autowired
    private MarketDataProvider dataProvider;

    @Autowired
    private MarketConfig marketConfig;

    @Autowired
    private FundManagementService fundManagementService;

    @Override
    public void simulate() {
        List<FundInfo> stocks = getStocks();
        Simulation simulation = new Simulation(stocks, marketConfig);

        simulation.setPurchaseStrategy(this::preparePurchaseModel);
        simulation.setSellStrategy(this::prepareSellModel);
        final LocalDate today = LocalDate.now();
        var p = simulation.simulate(200000, today.minusYears(5), today);
        p.print();
    }

    @Override
    public void simulateForDay(InvestmentProfile profile, LocalDate date) {
        List<FundInfo> stocks = getStocks();
        Simulation simulation = new Simulation(stocks, marketConfig);

        simulation.setPurchaseStrategy(this::preparePurchaseModel);
        simulation.setSellStrategy(this::prepareSellModel);
        simulation.simulate(profile, date);
        profile.print();
    }

    private TradeModel prepareSellModel(InvestmentProfile investmentProfile) {
        SellLastOnPercent model = new SellLastOnPercent(investmentProfile, dataProvider, TARGET_PERCENT);
        AbstractSelectionStrategy strategy = new RsiSellStrategy(dataProvider);
        model.setStrategy(strategy);
        return model;
    }

    private TradeModel preparePurchaseModel(InvestmentProfile investmentProfile) {
        PurchaseFresh model = new PurchaseFresh(investmentProfile, dataProvider, DAILY_FUND);
        RsiPurchaseStrategy strategy = new RsiPurchaseStrategy(dataProvider);
        final LimitedSelection limit = new LimitedSelection(MAX_SELECTION);
        model.setStrategy(strategy.setNext(limit));
        return model;
    }

    private List<FundInfo> getStocks() {
        return fundManagementService.getFundDetails().stream()
                .filter(i -> i.getMarketCap()!=null)
                .sorted(Comparator.comparing(FundInfo::getMarketCap).reversed()).limit(25).collect(Collectors.toList());
    }

    private static class RsiPurchaseStrategy extends GenericSelectionStrategy {

        public RsiPurchaseStrategy(MarketDataProvider dataProvider) {
            super(dataProvider);
        }

        @Override
        protected int getHistoryDays() {
            return 20;
        }

        @Override
        protected boolean isSelected(FundInfo info) {
            return true;
        }

        @Override
        protected boolean isSelected(FundWrapper wrapper) {
            var close = NumericIndicator.closePrice(wrapper.getSeries());
            var rsi = NumericIndicator.of(new RSIIndicator(close, 14));
            Rule r = rsi.isLessThan(35);

            return r.isSatisfied(wrapper.getSeries().getEndIndex());
        }

        @Override
        protected NumericIndicator getRanker(BarSeries series) {
            var close = NumericIndicator.closePrice(series);
            return NumericIndicator.of(new RSIIndicator(close, 14));
        }
    }

    private static class RsiSellStrategy extends GenericSelectionStrategy {

        public RsiSellStrategy(MarketDataProvider dataProvider) {
            super(dataProvider);
        }

        @Override
        protected int getHistoryDays() {
            return 20;
        }

        @Override
        protected boolean isSelected(FundInfo info) {
            return true;
        }

        @Override
        protected boolean isSelected(FundWrapper wrapper) {
            var close = NumericIndicator.closePrice(wrapper.getSeries());
            var rsi = NumericIndicator.of(new RSIIndicator(close, 14));
            Rule r = rsi.isGreaterThan(65);

            return r.isSatisfied(wrapper.getSeries().getEndIndex());
        }

        @Override
        protected NumericIndicator getRanker(BarSeries series) {
            var close = NumericIndicator.closePrice(series);
            return NumericIndicator.of(new RSIIndicator(close, 14)).multipliedBy(-1);
        }
    }

}

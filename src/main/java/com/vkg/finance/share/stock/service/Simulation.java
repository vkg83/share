package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.config.MarketConfig;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.InvestmentProfile;
import com.vkg.finance.share.stock.trade.TradeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public class Simulation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Simulation.class);
    private final List<FundInfo> stocks;
    private final MarketConfig marketConfig;
    private Function<InvestmentProfile, TradeModel> purchaseFunction;
    private Function<InvestmentProfile, TradeModel> sellFunction;

    public Simulation(List<FundInfo> stocks, MarketConfig marketConfig) {
        this.stocks = stocks;
        this.marketConfig = marketConfig;
    }

    public void setPurchaseStrategy(Function<InvestmentProfile, TradeModel> purchaseFunction) {
        this.purchaseFunction = purchaseFunction;
    }

    public void setSellStrategy(Function<InvestmentProfile, TradeModel> sellFunction) {
        this.sellFunction = sellFunction;
    }

    public InvestmentProfile simulate(long balance, LocalDate from, LocalDate to) {
        InvestmentProfile investmentProfile = new InvestmentProfile("test");
        investmentProfile.setBalance(balance);

        TradeModel purchaseModel = purchaseFunction.apply(investmentProfile);
        TradeModel sellModel = sellFunction.apply(investmentProfile);

        simulate(from, to, purchaseModel, sellModel);

        return investmentProfile;
    }

    public void simulate(LocalDate from, LocalDate to, TradeModel purchaseModel, TradeModel sellModel) {
        LocalDate curDate = from;

        while (curDate.isBefore(to)) {
            curDate = curDate.plusDays(1);
            if(marketConfig.isMarketClosed(curDate)) continue;
            LOGGER.info("Date : {}", curDate);
            purchaseModel.trade(stocks, curDate);
            sellModel.trade(stocks, curDate);
        }
    }
}

package com.vkg.finance.share.stock.trade;

import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.InvestmentProfile;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PurchaseFresh extends AbstractTradeModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseFresh.class);

    private final InvestmentProfile investmentProfile;
    private final MarketDataProvider dataProvider;
    private double amount;

    public PurchaseFresh(InvestmentProfile investmentProfile, MarketDataProvider dataProvider) {
        this.investmentProfile = investmentProfile;
        this.dataProvider = dataProvider;
        this.amount = 30000;
    }

    public PurchaseFresh(InvestmentProfile investmentProfile, MarketDataProvider dataProvider, double dailyFund) {
        this.investmentProfile = investmentProfile;
        this.dataProvider = dataProvider;
        this.amount = dailyFund;
    }

    @Override
    protected void performTrade(List<FundInfo> source, LocalDate date) {
        tryPurchase(source, date);
    }

    private boolean tryPurchase(List<FundInfo> fundInfos, LocalDate date) {
        if (fundInfos.isEmpty()) return false;
        final String symbol = fundInfos.get(0).getSymbol();
        if (investmentProfile.getInvestedQuantity(symbol) > 0) {
            return tryPurchase( fundInfos.subList(1, fundInfos.size()), date);
        } else {
            return purchase(symbol, date);
        }
    }

    private boolean purchase(String symbol, LocalDate date) {
        final Optional<FundHistory> history = dataProvider.getHistory(symbol, date);
        if (history.isEmpty()) {
            //LOGGER.warn("No current price available for {} on {} for purchase", symbol.getSymbol(), date);
            return false;
        }

        FundHistory h = history.get();
        double m = 1.022826628;
        try {
            investmentProfile.purchase(h, amount);
            amount *= m;
        } catch (Exception e) {
            LOGGER.warn("Skipping purchase on {}! {}", date, e.getMessage());
            return false;
        }

        return true;
    }

}

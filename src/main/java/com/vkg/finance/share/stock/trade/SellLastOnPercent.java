package com.vkg.finance.share.stock.trade;

import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.Investment;
import com.vkg.finance.share.stock.model.InvestmentProfile;
import com.vkg.finance.share.stock.repository.MarketDataProvider;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SellLastOnPercent extends AbstractTradeModel {

    private final double percent;
    private final InvestmentProfile investmentProfile;
    private final MarketDataProvider dataProvider;

    public SellLastOnPercent(InvestmentProfile investmentProfile, MarketDataProvider dataProvider, double percent) {
        this.investmentProfile = investmentProfile;
        this.dataProvider = dataProvider;
        this.percent = percent;
    }

    @Override
    protected void performTrade(List<FundInfo> source, LocalDate curDate) {
        double maxProfit = 0;

        FundHistory his = null;
        final Set<String> sym = investmentProfile.getInvestments().stream().map(Investment::getStockSymbol).collect(Collectors.toSet());
        final List<String> symbols = source.stream().map(FundInfo::getSymbol).filter(sym::contains).collect(Collectors.toList());
        for(String symbol : symbols) {
            final Optional<FundHistory> history = dataProvider.getHistory(symbol, curDate);
            if (history.isEmpty()) {
                //LOGGER.warn("No current price available for {} on {} for sale", i.getStockSymbol(), curDate);
                continue;
            }
            FundHistory h = history.get();
            double price = h.getLastTradedPrice();
            Investment inv = investmentProfile.getLastInvestment(h.getSymbol());
            double am = inv.getAmount();
            int count = investmentProfile.getInvestedCount(h.getSymbol());
            int qty = investmentProfile.getInvestedQuantity(h.getSymbol());
            double target = getTarget(count);
            if(inv.getPrice() * target < price) {
                double profit = price * qty - am;
                if(maxProfit < profit) {
                    maxProfit = profit;
                    his = h;
                }
            }
        }

        if(his != null) {
            investmentProfile.sellLast(his);
        }
    }

    private double getTarget(int count) {
        double target = percent / 100.0;
        if(count == 4) {
            target *= 2;
        } else if(count > 4) {
            target *= 3;
        }
        target += 1;
        return target;
    }

}

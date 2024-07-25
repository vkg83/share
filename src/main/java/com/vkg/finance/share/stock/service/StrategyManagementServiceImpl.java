package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.SelectionStrategyRequest;
import com.vkg.finance.share.stock.model.FundType;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import com.vkg.finance.share.stock.strategies.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StrategyManagementServiceImpl implements StrategyManagementService {
    @Autowired
    private MarketDataProvider dataProvider;

    @Override
    public List<FundInfo> applyMovingAverage(SelectionStrategyRequest request) {
        final List<FundInfo> allFundInfos = loadFunds(request.getSymbols());
        //implementation needed
        MovingAverageStrategy strategy = new MovingAverageStrategy(dataProvider);
        return strategy.select(allFundInfos, LocalDate.now());
    }

    private List<FundInfo> loadFunds(List<String> symbols) {
        return dataProvider.getAllFunds(FundType.ETF).stream()
                .filter(f-> symbols.contains(f.getSymbol()))
                .toList();
    }

    @Override
    public boolean applyDarvos(SelectionStrategyRequest selectionStrategyRequest) {
        DarvosTradingStrategy strategy = new DarvosTradingStrategy(dataProvider);
        return false; //strategy.buy(fund);
    }
}

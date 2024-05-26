package com.vkg.finance.share.stock;


import com.vkg.finance.share.stock.client.FundDataProvider;
import com.vkg.finance.share.stock.model.Fund;
import com.vkg.finance.share.stock.model.FundType;
import com.vkg.finance.share.stock.strategies.MovingAverageStrategy;
import com.vkg.finance.share.stock.strategies.SelectionStrategy;
import com.vkg.finance.share.stock.strategies.SimpleFundSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NSEService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NSEService.class);

    private static final int MIN_ALLOWED_VOLUME = 5000;
    @Autowired
    private FundDataProvider dataProvider;

    public List<Fund> loadEtfInfo() {
        LOGGER.info("Analyzed Etf");
        final List<Fund> allFunds = dataProvider.getAllFunds(FundType.ETF);
        var s = new SimpleFundSelector()
                .setMinVolume(MIN_ALLOWED_VOLUME).excludeAssets("GOLD", "SILVER", "LIQUID");
        SelectionStrategy strategy = new MovingAverageStrategy(dataProvider);
        s.setNext(strategy);
        return s.select(allFunds);
    }

    public List<Fund> loadJwelInfo() {
        LOGGER.info("Analyzed Gold and Silver");
        final List<Fund> allFunds = dataProvider.getAllFunds(FundType.ETF);
        var s = new SimpleFundSelector()
                .setMinVolume(MIN_ALLOWED_VOLUME).includeAssets("GOLD", "SILVER");
        SelectionStrategy strategy = new MovingAverageStrategy(dataProvider);
        s.setNext(strategy);
        return s.select(allFunds);
    }
}

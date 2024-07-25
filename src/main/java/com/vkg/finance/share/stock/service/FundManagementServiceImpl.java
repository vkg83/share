package com.vkg.finance.share.stock.service;


import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundType;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import com.vkg.finance.share.stock.repository.FundDetailDao;
import com.vkg.finance.share.stock.strategies.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FundManagementServiceImpl implements FundManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FundManagementServiceImpl.class);

    private static final int MIN_ALLOWED_VOLUME = 5000;
    @Autowired
    private MarketDataProvider dataProvider;
    @Autowired
    private FundDetailDao fundDetailDao;

    public List<FundInfo> loadEtfInfo() {
        LOGGER.info("Analyzed Etf");
        final List<FundInfo> allFundInfos = dataProvider.getAllFunds(FundType.ETF);
        var s = new SimpleFundSelector(dataProvider)
                .setMinVolume(MIN_ALLOWED_VOLUME).excludeAssets("GOLD", "SILVER", "LIQUID");
        SelectionStrategy strategy = new MovingAverageStrategy(dataProvider);
        s.setNext(strategy);
        return s.select(allFundInfos, LocalDate.now());
    }

    public List<FundInfo> loadJwelInfo() {
        LOGGER.info("Analyzed Gold and Silver");
        final List<FundInfo> allFundInfos = dataProvider.getAllFunds(FundType.ETF);
        var s = new SimpleFundSelector(dataProvider)
                .setMinVolume(MIN_ALLOWED_VOLUME).includeAssets("GOLD", "SILVER");
        SelectionStrategy strategy = new MovingAverageStrategy(dataProvider);
        s.setNext(strategy);
        return s.select(allFundInfos, LocalDate.now());
    }

    public List<FundInfo> applyDarvos() {
        LOGGER.info("Analyzed Gold and Silver");
        final List<FundInfo> allFundInfos = loadEtfInfo();
        DarvosTradingStrategy strategy = new DarvosTradingStrategy(dataProvider);
        RedGreenStrategy s = new RedGreenStrategy(dataProvider);
        strategy.setNext(s);
        return allFundInfos.stream().filter(i -> strategy.buy(i) != null).collect(Collectors.toList());
    }

    @Override
    public void clearCache() {
        dataProvider.clearCache();
    }

    @Override
    public List<FundInfo> getAllEtfs() {
        return dataProvider.getAllFunds(FundType.ETF);
    }

    @Override
    public void updateFundDetails(List<FundInfo> fundInfoList) {
        Map<String, FundInfo> savedFundInfo = fundDetailDao.loadAll().stream().collect(Collectors.toMap(FundInfo::getSymbol, Function.identity()));

        for (FundInfo f : fundInfoList) {
            FundInfo fund = savedFundInfo.get(f.getSymbol());
            if(fund == null) {
                savedFundInfo.put(f.getSymbol(), f);
            } else {
                fund.setName(f.getName());
                fund.setMarketCap(f.getMarketCap());
            }
        }

        fundDetailDao.saveAll(savedFundInfo.values());
    }

    @Override
    public List<FundInfo> getFundDetails() {
        return fundDetailDao.loadAll();
    }
}

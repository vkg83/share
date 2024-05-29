package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.client.FundDataProvider;
import com.vkg.finance.share.stock.model.Fund;
import com.vkg.finance.share.stock.model.FundWithHistory;
import com.vkg.finance.share.stock.model.FundHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MovingAverageStrategy extends AbstractSelectionStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovingAverageStrategy.class);

    private FundDataProvider dataProvider;

    public MovingAverageStrategy(FundDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public List<Fund> execute(List<Fund> funds) {
        return analyzeFunds(funds);
    }

    private List<Fund> analyzeFunds(List<Fund> funds) {
        funds.sort(Comparator.comparing(Fund::getSymbol));
        LOGGER.info("+---------------------------------------------------------------------------------------------------------+");
        LOGGER.info("|                                           Order By Name                                                 |");
        LOGGER.info("+---------------------------------------------------------------------------------------------------------+");
        for (int i = 0; i < funds.size(); i++) {
            Fund v = funds.get(i);
            LOGGER.info(String.format("| %3d | %-15s | %15d | %8.2f | %-50s |", i + 1, v.getSymbol(), v.getVolume(), v.getLastTradingPrice(), v.getAssets()));
        }
        LOGGER.info("+---------------------------------------------------------------------------------------------------------+");

        funds.sort(Comparator.comparing(Fund::getVolume).reversed());

        LOGGER.info("+---------------------------------------------------------------+");
        LOGGER.info("|                        Order by Volume                        |");
        LOGGER.info("+---------------------------------------------------------------+");
        List<FundWithHistory> fundAnalyses = new ArrayList<>();
        for (int i = 0; i < funds.size(); i++) {
            final Fund fund = funds.get(i);
            List<FundHistory> fundHistory = dataProvider.getHistory(fund, 20);
            FundWithHistory analysis = new FundWithHistory(fund, fundHistory);
            fundAnalyses.add(analysis);

            LOGGER.info(String.format("| %3d | Got History of: %-15s | %8.2f %% | %8d |", i + 1, fund.getSymbol(), analysis.getVolumeChangePercent(), fund.getVolume()));
        }
        LOGGER.info("+---------------------------------------------------------------+");

        fundAnalyses.sort(Comparator.comparing(FundWithHistory::getVolumeChangePercent));
        final double meanVolume = fundAnalyses.get(fundAnalyses.size() / 2).getVolumeChangePercent();
        LOGGER.info(String.format("Average volume change: %8.2f %%", meanVolume));

        fundAnalyses.sort(Comparator.comparingDouble(FundWithHistory::getPriceChangePercent));
        LOGGER.info("+------------------------------------------------------------+");
        LOGGER.info("|                       Ranking Result                       |");
        LOGGER.info("+------------------------------------------------------------+");
        for (int i = 0; i < funds.size() && i < 10; i++) {
            FundWithHistory a = fundAnalyses.get(i);
            Fund v = a.getFund();
            LOGGER.info(String.format("| %3d | %-15s | %8.2f %% | %8.2f | %10s |", i + 1, v.getSymbol(), a.getPriceChangePercent(), v.getLastTradingPrice(), v.getActionDate()));
        }
        LOGGER.info("+------------------------------------------------------------+");
        return fundAnalyses.subList(0, Math.min(10, fundAnalyses.size())).stream()
                .map(FundWithHistory::getFund).collect(Collectors.toList());
    }

}

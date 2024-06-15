package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.client.FundDataProvider;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundWithHistory;
import com.vkg.finance.share.stock.model.FundHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MovingAverageStrategy extends AbstractSelectionStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovingAverageStrategy.class);

    private final FundDataProvider dataProvider;
    private LocalDate currentDate;
    private int historyDays;
    private int maxResultCount;

    public MovingAverageStrategy(FundDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.currentDate = LocalDate.now();
        this.historyDays = 20;

    }

    public void setHistoryDays(int historyDays) {
        this.historyDays = historyDays;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public void setMaxResultCount(int maxResultCount) {
        this.maxResultCount = maxResultCount;
    }

    @Override
    public List<FundInfo> execute(List<FundInfo> fundInfos) {
        return analyzeFunds(fundInfos);
    }

    private List<FundInfo> analyzeFunds(List<FundInfo> fundInfos) {
        //funds.sort(Comparator.comparing(Fund::getSymbol));
//        LOGGER.info("+---------------------------------------------------------------------------------------------------------+");
//        LOGGER.info("|                                           Order By Name                                                 |");
//        LOGGER.info("+---------------------------------------------------------------------------------------------------------+");
//        for (int i = 0; i < funds.size(); i++) {
//            Fund v = funds.get(i);
//            LOGGER.info(String.format("| %3d | %-15s | %15d | %8.2f | %-50s |", i + 1, v.getSymbol(), v.getVolume(), v.getLastTradingPrice(), v.getAssets()));
//        }
//        LOGGER.info("+---------------------------------------------------------------------------------------------------------+");

//        funds.sort(Comparator.comparing(Fund::getVolume).reversed());

//        LOGGER.info("+---------------------------------------------------------------+");
//        LOGGER.info("|                        Order by Volume                        |");
//        LOGGER.info("+---------------------------------------------------------------+");
        List<FundWithHistory> fundAnalyses = new ArrayList<>();
        List<String> symbols = new ArrayList<>();
        for (final FundInfo fundInfo : fundInfos) {
            List<FundHistory> fundHistory = dataProvider.getHistory(fundInfo.getSymbol(), currentDate, historyDays + 1);
            if (fundHistory.size() < historyDays + 1) {
                symbols.add(fundInfo.getSymbol());
                continue;
            }
            FundWithHistory analysis = new FundWithHistory(fundInfo, fundHistory);
            fundAnalyses.add(analysis);

            //LOGGER.info(String.format("| %3d | Got History of: %-15s | %8.2f %% | %8d |", i + 1, fund.getSymbol(), analysis.getVolumeChangePercent(), fund.getVolume()));
        }
        //LOGGER.info("No history available for {} on {}", symbols, toDate);
//        LOGGER.info("+---------------------------------------------------------------+");
//        fundAnalyses.sort(Comparator.comparing(FundWithHistory::getVolumeChangePercent));
//        final double meanVolume = fundAnalyses.get(fundAnalyses.size() / 2).getVolumeChangePercent();
//        LOGGER.info(String.format("Average volume change: %8.2f %%", meanVolume));

        fundAnalyses.sort(Comparator.comparingDouble(FundWithHistory::getPriceChangePercent));
//        LOGGER.info("+------------------------------------------------------------+");
//        LOGGER.info("|                       Ranking Result                       |");
//        LOGGER.info("+------------------------------------------------------------+");
        for (int i = 0; i < fundAnalyses.size() && i < 10; i++) {
            FundWithHistory a = fundAnalyses.get(i);
            FundInfo v = a.getFund();
//            LOGGER.info(String.format("| %3d | %-15s | %8.2f %% | %8.2f | %10s |", i + 1, v.getSymbol(), a.getPriceChangePercent(), v.getLastTradingPrice(), v.getActionDate()));
        }
//        LOGGER.info("+------------------------------------------------------------+");
        return fundAnalyses.subList(0, Math.min(10, fundAnalyses.size())).stream()
                .map(FundWithHistory::getFund).collect(Collectors.toList());
    }

}

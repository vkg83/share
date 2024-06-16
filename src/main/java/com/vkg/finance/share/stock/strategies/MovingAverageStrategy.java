package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundWithHistory;
import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MovingAverageStrategy extends AbstractSelectionStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovingAverageStrategy.class);

    private final MarketDataProvider dataProvider;
    private LocalDate currentDate;
    private int historyDays;
    private int maxResultCount;

    public MovingAverageStrategy(MarketDataProvider dataProvider) {
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
        List<FundWithHistory> fundAnalyses = new ArrayList<>();
        List<String> noHistorySymbols = new ArrayList<>();
        for (final FundInfo fundInfo : fundInfos) {
            List<FundHistory> fundHistory = dataProvider.getHistory(fundInfo.getSymbol(), currentDate, historyDays + 1);
            if (fundHistory.size() < historyDays + 1) {
                noHistorySymbols.add(fundInfo.getSymbol());
                continue;
            }
            FundWithHistory analysis = new FundWithHistory(fundInfo, fundHistory);
            fundAnalyses.add(analysis);
        }

//        if(!noHistorySymbols.isEmpty()) {
//            LOGGER.warn("No history available for {} on {}", noHistorySymbols, currentDate);
//        }

        fundAnalyses.sort(Comparator.comparingDouble(FundWithHistory::getPriceChangePercent));
        LOGGER.debug("+------------------------------------------------------------+");
        LOGGER.debug("|                       Ranking Result                       |");
        LOGGER.debug("+------------------------------------------------------------+");
        for (int i = 0; i < fundAnalyses.size(); i++) {
            FundWithHistory a = fundAnalyses.get(i);
            FundInfo v = a.getFund();
            String history = a.getFundHistory().stream().map(f -> Double.toString(f.getClosingPrice())).collect(Collectors.joining(", "));
            LOGGER.debug(String.format("| %3d | %-15s | %4.4f %% | %s | %10s |", i + 1, v.getSymbol(), a.getPriceChangePercent(), history, v.getActionDate()));
        }
        LOGGER.debug("+------------------------------------------------------------+");
        return fundAnalyses.subList(0, Math.min(10, fundAnalyses.size())).stream()
                .map(FundWithHistory::getFund).collect(Collectors.toList());
    }

}

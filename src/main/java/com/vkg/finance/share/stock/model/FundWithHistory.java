package com.vkg.finance.share.stock.model;

import java.util.Arrays;
import java.util.List;

public class FundWithHistory {
    private final FundInfo fundInfo;
    private final List<FundHistory> fundHistory;

    public FundWithHistory(FundInfo fundInfo, List<FundHistory> fundHistory) {
        this.fundInfo = fundInfo;
        this.fundHistory = fundHistory;
    }

    public double getPriceChangePercent() {
        final double averagePrice = getAveragePrice();
        final double price = fundHistory.get(0).getClosingPrice();
        return (price - averagePrice) * 100.0 / averagePrice;
    }

    public double getVolumeChangePercent() {
        long meanVolume = getMeanVolume();
        final long volume = fundHistory.get(0).getVolume();
        return (volume - meanVolume) * 100.0 / meanVolume;
    }

    public List<FundHistory> getFundHistory() {
        return fundHistory;
    }

    public FundInfo getFund() {
        return fundInfo;
    }

    public double getAveragePrice() {
        double sum = 0;
        int count = 0;
        for (int i = 1; i < fundHistory.size(); i++) {
            FundHistory info = fundHistory.get(i);
            sum += info.getClosingPrice();
            count++;
        }
        return count == 0 ? 0 : sum/count;
    }

    public long getMeanVolume() {
        long[] sum = new long[fundHistory.size()];
        for (int i = 0; i < fundHistory.size(); i++) {
            FundHistory info = fundHistory.get(i);
            sum[i] = info.getVolume();
        }
        Arrays.sort(sum);
        return sum[sum.length/2];
    }

}

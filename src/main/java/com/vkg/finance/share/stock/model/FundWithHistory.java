package com.vkg.finance.share.stock.model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class FundWithHistory {
    private final Fund fund;
    private final List<FundHistory> fundHistory;

    public FundWithHistory(Fund fund, List<FundHistory> fundHistory) {
        this.fund = fund;
        this.fundHistory = fundHistory;
    }

    public double getPriceChangePercent() {
        final double averagePrice = getAveragePrice(fund.getActionDate());
        return (fund.getLastTradingPrice() - averagePrice) * 100.0 / averagePrice;
    }

    public double getVolumeChangePercent() {
        long meanVolume = getMeanVolume();
        return (fund.getVolume() - meanVolume) * 100.0 / meanVolume;
    }

    public Fund getFund() {
        return fund;
    }

    public double getAveragePrice(LocalDate actDate) {
        double sum = 0;
        int count = 0;
        LocalDate date = LocalDate.now();
        for (FundHistory info : fundHistory) {
            if(date.equals(info.getDate())) continue;
            if(actDate != null && info.getDate().isBefore(actDate))
                break;
            sum += info.getLastTradedPrice();
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

package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class LimitedSelection extends AbstractSelectionStrategy {
    private final int maxResultCount;

    public LimitedSelection(int maxResultCount) {
        this.maxResultCount = maxResultCount;
    }

    @Override
    protected List<FundInfo> execute(List<FundInfo> fundInfos, LocalDate date) {
        return fundInfos.stream().limit(maxResultCount).collect(Collectors.toList());
    }
}

package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundInfo;

import java.time.LocalDate;
import java.util.List;

public interface SelectionStrategy {
    List<FundInfo> select(List<FundInfo> fundInfos, LocalDate date);
}

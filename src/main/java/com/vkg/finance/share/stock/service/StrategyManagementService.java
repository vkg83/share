package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.SelectionStrategyRequest;

import java.util.List;

public interface StrategyManagementService {

    List<FundInfo> applyMovingAverage(SelectionStrategyRequest selectionStrategyRequest);

    boolean applyDarvos(SelectionStrategyRequest selectionStrategyRequest);
}

package com.vkg.finance.share.stock.client;

import com.vkg.finance.share.stock.model.Fund;
import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.model.FundType;

import java.util.List;

public interface FundDataProvider {

    List<Fund> getAllFunds(FundType type);

    List<FundHistory> getHistory(Fund fund);
}

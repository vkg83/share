package com.vkg.finance.share.stock.client;

import com.vkg.finance.share.stock.model.Fund;
import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.model.FundType;

import java.time.LocalDate;
import java.util.List;

public interface FundDataProvider {

    List<Fund> getAllFunds(FundType type);

    List<FundHistory> getHistory(Fund fund, int days);

    List<FundHistory> getHistory(Fund fund, LocalDate start, LocalDate end);

}

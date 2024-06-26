package com.vkg.finance.share.stock.repository;

import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarketDataProvider {

    List<FundInfo> getAllFunds(FundType type);

    List<FundHistory> getHistory(String symbol, LocalDate date, int days);

    List<FundHistory> getHistory(String symbol, LocalDate start, LocalDate end);

    Optional<FundHistory> getHistory(String symbol, LocalDate date);

    void clearCache();

}

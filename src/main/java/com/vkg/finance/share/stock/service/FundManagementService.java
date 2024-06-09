package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.model.FundInfo;

import java.util.List;

public interface FundManagementService {

    void updateFundDetails(List<FundInfo> extract);

    List<FundInfo> getFundDetails();

    void clearCache();

    List<FundInfo> getAllEtfs();

}

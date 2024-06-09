package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.model.InvestmentProfile;
import com.vkg.finance.share.stock.model.Investment;

public interface InvestmentProfileService {

    InvestmentProfile createProfile(String profileName);

    void addInvestment(String profileName, Investment investment);

    InvestmentProfile getProfile(String profileName);
}

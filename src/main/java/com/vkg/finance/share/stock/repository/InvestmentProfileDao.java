package com.vkg.finance.share.stock.repository;

import com.vkg.finance.share.stock.model.InvestmentProfile;

public interface InvestmentProfileDao {
    void save(InvestmentProfile profile);

    InvestmentProfile load(String profileName);
}

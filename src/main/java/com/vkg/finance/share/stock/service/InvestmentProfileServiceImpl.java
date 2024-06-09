package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.model.InvestmentProfile;
import com.vkg.finance.share.stock.model.Investment;
import com.vkg.finance.share.stock.repository.InvestmentProfileDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvestmentProfileServiceImpl implements InvestmentProfileService {
    @Autowired
    private InvestmentProfileDao dao;
    @Override
    public InvestmentProfile createProfile(String profileName) {
        var profile = new InvestmentProfile(profileName);
        dao.save(profile);
        return profile;
    }

    @Override
    public void addInvestment(String profileName, Investment investment) {
        var profile = dao.load(profileName);
        var investments = profile.getInvestments();
        investments.add(investment);
        dao.save(profile);
    }

    @Override
    public InvestmentProfile getProfile(String profileName) {
        return dao.load(profileName);
    }
}

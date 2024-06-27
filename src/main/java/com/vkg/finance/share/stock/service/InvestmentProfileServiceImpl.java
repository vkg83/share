package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.model.InvestmentProfile;
import com.vkg.finance.share.stock.repository.InvestmentProfileDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class InvestmentProfileServiceImpl implements InvestmentProfileService {
    @Autowired
    private InvestmentProfileDao dao;
    @Override
    public InvestmentProfile createProfile(String profileName, double balance) {
        var profile = new InvestmentProfile(profileName);
        profile.setBalance(balance);
        dao.save(profile);
        return profile;
    }

    @Override
    public void purchase(String profileName, String symbol, LocalDate date, int quantity, double price) {
        var profile = dao.load(profileName);
        profile.purchase(symbol, date, price, quantity);
        dao.save(profile);
    }

    @Override
    public InvestmentProfile getProfile(String profileName) {
        return dao.load(profileName);
    }

    @Override
    public void sellLast(String profileName, String symbol, LocalDate date, double price) {
        var profile = dao.load(profileName);
        FundHistory history = new FundHistory();
        history.setSymbol(symbol);
        history.setDate(date);
        history.setLastTradedPrice(price);
        profile.sellLast(history);
        dao.save(profile);
    }
}

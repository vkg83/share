package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.model.InvestmentProfile;

import java.time.LocalDate;

public interface InvestmentProfileService {

    InvestmentProfile createProfile(String profileName, double balance);

    void purchase(String profileName, String symbol, LocalDate date, int quantity, double price);

    InvestmentProfile getProfile(String profileName);
}

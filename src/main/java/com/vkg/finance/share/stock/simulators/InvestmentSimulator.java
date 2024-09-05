package com.vkg.finance.share.stock.simulators;

import com.vkg.finance.share.stock.model.InvestmentProfile;

import java.time.LocalDate;

public interface InvestmentSimulator {

    void simulate();

    void simulateForDay(InvestmentProfile p, LocalDate today);

}

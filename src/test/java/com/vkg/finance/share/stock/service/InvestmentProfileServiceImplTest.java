package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.repository.FileBasedFundDetailDao;
import com.vkg.finance.share.stock.repository.FileBasedInvestmentProfileDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {InvestmentProfileServiceImpl.class, FileBasedInvestmentProfileDao.class, FileBasedFundDetailDao.class})
@EnableConfigurationProperties
class InvestmentProfileServiceImplTest {
    @Autowired
    private InvestmentProfileService investmentProfileService;
    @Test
    void saveInvestmentNehaEtf() {
        var date = LocalDate.now();
        //investmentProfileService.sellLast("NEHA_ETF_SHOP", "TITAN", date, 3720);
        //investmentProfileService.purchase("NEHA_ETF_SHOP", "PSUBANKADD", date, 69, 73.24);
        //investmentProfileService.purchase("NEHA_ETF_SHOP", "SILVRETF", date, 58, 86.50);
        //investmentProfileService.purchase("NEHA_ETF_SHOP", "MARUTI", date, 1, 12177.35);
        System.out.println("Saved");
    }

    @Test
    void saveInvestmentVkgRSI() {
        var date = LocalDate.now();
        //investmentProfileService.sellLast("VKG_RSI_SHOP", "TITAN", date, 3720);
        //investmentProfileService.purchase("VKG_RSI_SHOP", "SBIN", date, 7, 781.05);
        System.out.println("Saved");
    }

    @Test
    void printProfileForNeha() {
        var profile = investmentProfileService.getProfile("NEHA_ETF_SHOP");
        profile.print();
    }

    @Test
    void printProfileForVkg() {
        var profile = investmentProfileService.getProfile("VKG_RSI_SHOP");
        profile.print();
    }
}
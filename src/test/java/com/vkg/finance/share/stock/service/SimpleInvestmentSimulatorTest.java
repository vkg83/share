package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.InvestmentProfile;
import com.vkg.finance.share.stock.repository.FileBasedFundDetailDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Comparator;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SimpleInvestmentSimulator.class, FundManagementServiceImpl.class, FileBasedFundDetailDao.class})
@EnableConfigurationProperties
class SimpleInvestmentSimulatorTest {
    @Autowired
    SimpleInvestmentSimulator unit;
    @Autowired
    private FundManagementService fundManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldFindTopMarketCapStock() {
        fundManagementService.getFundDetails().stream()
                .filter(i -> i.getMarketCap()!=null)
                .sorted(Comparator.comparing(FundInfo::getMarketCap).reversed()).limit(50)
                .forEach(i -> System.out.println(i.getSymbol()));
    }

    @Test
    void shouldDoLifo() {
        InvestmentProfile p = new InvestmentProfile("sim_etf");
        p.setBalance(400000);
        unit.doLifoShop(p);
    }

    @Test
    void shouldSimulate() {
        unit.simulateEtfShop();
    }

    @Test
    void shouldSimulateLifo() {
        unit.simulateLifoShop();
    }

}
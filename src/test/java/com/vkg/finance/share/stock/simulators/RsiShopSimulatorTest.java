package com.vkg.finance.share.stock.simulators;

import com.vkg.finance.share.stock.client.NSEJSoupClient;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.InvestmentProfile;
import com.vkg.finance.share.stock.repository.FileBasedFundDetailDao;
import com.vkg.finance.share.stock.repository.FileBasedInvestmentProfileDao;
import com.vkg.finance.share.stock.repository.MarketDataProviderImpl;
import com.vkg.finance.share.stock.service.FundManagementService;
import com.vkg.finance.share.stock.service.FundManagementServiceImpl;
import com.vkg.finance.share.stock.service.InvestmentProfileService;
import com.vkg.finance.share.stock.service.InvestmentProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Comparator;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {RsiShopSimulator.class, FundManagementServiceImpl.class,
        InvestmentProfileServiceImpl.class, FileBasedInvestmentProfileDao.class,
        FileBasedFundDetailDao.class, MarketDataProviderImpl.class, NSEJSoupClient.class})
@EnableConfigurationProperties
class RsiShopSimulatorTest {
    @Autowired
    RsiShopSimulator unit;
    @Autowired
    private FundManagementService fundManagementService;
    @Autowired
    private InvestmentProfileService investmentProfileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSimulateRsi() {
        unit.simulate();
    }
    @Test
    void shouldDoRsi() {
        final LocalDate today = LocalDate.now();
        var p = investmentProfileService.getProfile("NEHA_ETF_SHOP");
        unit.simulateForDay(p, today);
    }
}
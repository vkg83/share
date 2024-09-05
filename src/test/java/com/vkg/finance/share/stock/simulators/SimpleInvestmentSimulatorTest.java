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
@SpringBootTest(classes = {SimpleInvestmentSimulator.class, FundManagementServiceImpl.class,
        InvestmentProfileServiceImpl.class, FileBasedInvestmentProfileDao.class,
        FileBasedFundDetailDao.class, MarketDataProviderImpl.class, NSEJSoupClient.class})
@EnableConfigurationProperties
class SimpleInvestmentSimulatorTest {
    @Autowired
    SimpleInvestmentSimulator unit;
    @Autowired
    private FundManagementService fundManagementService;
    @Autowired
    private InvestmentProfileService investmentProfileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldFindTopMarketCapStock() {
        fundManagementService.getFundDetails().stream()
                .filter(i -> i.getMarketCap()!=null)
                .sorted(Comparator.comparing(FundInfo::getMarketCap).reversed()).limit(50)
                .forEach(i -> System.out.printf("%-10s | %,12.0f %n", i.getSymbol(), i.getMarketCap()));
    }

    @Test
    void shouldSimulateDarvas() {
        InvestmentProfile p = new InvestmentProfile("sim_darvas");
        p.setBalance(100000);
        FundInfo info = new FundInfo();
        info.setSymbol("HDFCBANK");
        unit.simulateDarvos(p, info);
    }

    @Test
    void shouldSimulateTA() {
        unit.simulate();
    }
    @Test
    void shouldDoTA() {
        final LocalDate today = LocalDate.now();
        var p = investmentProfileService.getProfile("NEHA_ETF_SHOP");
        unit.simulateForDay(p, today);
    }
}
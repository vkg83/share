package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.client.NSEJSoupClient;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.Investment;
import com.vkg.finance.share.stock.model.InvestmentProfile;
import com.vkg.finance.share.stock.repository.FileBasedFundDetailDao;
import com.vkg.finance.share.stock.repository.FileBasedInvestmentProfileDao;
import com.vkg.finance.share.stock.repository.MarketDataProviderImpl;
import com.vkg.finance.share.stock.util.FileUtil;
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
                .forEach(i -> System.out.println(i.getSymbol()));
    }

    @Test
    void saveInvestment() {
        var date = LocalDate.now();
        investmentProfileService.purchase("NEHA_ETF_SHOP", "MAHKTECH", date, 357, 14.06);
        investmentProfileService.purchase("NEHA_ETF_SHOP", "SILVER1", date, 56, 89.69);
        investmentProfileService.purchase("NEHA_ETF_SHOP", "LT", date, 2, 3532.25);
        System.out.println("Saved");
    }

    @Test
    void shouldDoLifo() {
        FileUtil.removeCurrent();
        var p = investmentProfileService.getProfile("NEHA_ETF_SHOP");
        unit.doLifoShop(p, LocalDate.now());
    }

    @Test
    void shouldSimulate() {
        unit.simulateEtfShop();
    }

    @Test
    void shouldSimulateLifo() {
        unit.simulateLifoShop();
    }

    @Test
    void shouldSimulateDarvos() {
        InvestmentProfile p = new InvestmentProfile("sim_darvos");
        p.setBalance(100000);
        FundInfo info = new FundInfo();
        info.setSymbol("HDFCBANK");
        unit.simulateDarvos(p, info);
    }

}
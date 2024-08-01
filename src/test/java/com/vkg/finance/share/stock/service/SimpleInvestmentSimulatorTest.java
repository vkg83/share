package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.client.NSEJSoupClient;
import com.vkg.finance.share.stock.model.FundInfo;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Path;
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
    @Value("${rest.cache.path}")
    private Path cacheBasePath;

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
    void saveInvestment() {
        var date = LocalDate.now();
        //investmentProfileService.sellLast("NEHA_ETF_SHOP", "RELIANCE", date, 3071.75);
        //investmentProfileService.purchase("NEHA_ETF_SHOP", "PSUBANKADD", date, 69, 73.24);
        //investmentProfileService.purchase("NEHA_ETF_SHOP", "SILVRETF", date, 58, 86.50);
        //investmentProfileService.purchase("NEHA_ETF_SHOP", "MARUTI", date, 1, 12177.35);
        System.out.println("Saved");
    }

    @Test
    void shouldDoLifo() throws IOException {
        final LocalDate today = LocalDate.now();
        FileUtil.removeCurrent(cacheBasePath.resolve(today.toString()));
        var p = investmentProfileService.getProfile("NEHA_ETF_SHOP");
        unit.doLifoShop(p, today);
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
    void shouldSimulateDarvas() {
        InvestmentProfile p = new InvestmentProfile("sim_darvas");
        p.setBalance(100000);
        FundInfo info = new FundInfo();
        info.setSymbol("HDFCBANK");
        unit.simulateDarvos(p, info);
    }

    @Test
    void shouldSimulateUsingTA() {
        unit.simulate();
    }
    @Test
    void shouldDoUsingTA() {
        final LocalDate today = LocalDate.now();
        var p = investmentProfileService.getProfile("NEHA_ETF_SHOP");
        unit.simulateOne(p, today);
    }
}
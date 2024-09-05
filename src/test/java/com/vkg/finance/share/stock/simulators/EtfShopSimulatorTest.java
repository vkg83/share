package com.vkg.finance.share.stock.simulators;

import com.vkg.finance.share.stock.client.NSEJSoupClient;
import com.vkg.finance.share.stock.repository.FileBasedFundDetailDao;
import com.vkg.finance.share.stock.repository.FileBasedInvestmentProfileDao;
import com.vkg.finance.share.stock.repository.MarketDataProviderImpl;
import com.vkg.finance.share.stock.service.FundManagementServiceImpl;
import com.vkg.finance.share.stock.service.InvestmentProfileService;
import com.vkg.finance.share.stock.service.InvestmentProfileServiceImpl;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {EtfShopSimulator.class, FundManagementServiceImpl.class,
        InvestmentProfileServiceImpl.class, FileBasedInvestmentProfileDao.class,
        FileBasedFundDetailDao.class, MarketDataProviderImpl.class, NSEJSoupClient.class})
@EnableConfigurationProperties
class EtfShopSimulatorTest {
    @Autowired
    EtfShopSimulator unit;
    @Autowired
    private InvestmentProfileService investmentProfileService;
    @Value("${rest.cache.path}")
    private Path cacheBasePath;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldDoEtf() throws IOException {
        final LocalDate today = LocalDate.now();
        FileUtil.removeCurrent(cacheBasePath.resolve(today.toString()));
        var p = investmentProfileService.getProfile("NEHA_ETF_SHOP");
        unit.simulateForDay(p, today);
    }

    @Test
    void shouldSimulateEtf() {
        unit.simulate();
    }

}
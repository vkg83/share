package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.client.NSEJSoupClient;
import com.vkg.finance.share.stock.repository.FileBasedFundDetailDao;
import com.vkg.finance.share.stock.repository.FileBasedInvestmentProfileDao;
import com.vkg.finance.share.stock.repository.MarketDataProviderImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {InvestmentProfileServiceImpl.class, FileBasedInvestmentProfileDao.class, FileBasedFundDetailDao.class, MarketDataProviderImpl.class, NSEJSoupClient.class})
class InvestmentProfileServiceImplTest {

}
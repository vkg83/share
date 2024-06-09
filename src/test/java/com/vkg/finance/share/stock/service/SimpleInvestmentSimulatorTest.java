package com.vkg.finance.share.stock.service;

import com.vkg.finance.share.stock.client.FundDataProvider;
import com.vkg.finance.share.stock.client.NSEJsoupClient;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SimpleInvestmentSimulator.class, NSEJsoupClient.class})
@EnableConfigurationProperties
class SimpleInvestmentSimulatorTest {
    @Autowired
    SimpleInvestmentSimulator unit;
    @Autowired
    FundDataProvider dataProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.with(IsoFields.DAY_OF_QUARTER, 1);
        System.out.println("Q2: " + from + " to " + to);
        to = from.minusDays(1);
        from = to.with(IsoFields.DAY_OF_QUARTER, 1);
        System.out.println("Q1: " + from + " to " + to);
    }

    @Test
    void shouldAct() {
        final List<FundInfo> allFunds = dataProvider.getAllFunds(FundType.ETF);
        allFunds.stream().filter(f-> !"-".equals(f.getCorporateAction())).filter(f-> f.getActionDate().isAfter(LocalDate.now().minusYears(1)))
                .forEach(f -> System.out.printf("%s: %s - %s%n", f.getSymbol(), f.getActionDate(), f.getCorporateAction()));
    }

    @Test
    void shouldSimulate() {
        unit.simulateEtfShop();
    }

}
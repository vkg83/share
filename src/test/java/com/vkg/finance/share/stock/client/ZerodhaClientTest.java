package com.vkg.finance.share.stock.client;

import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Holding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.List;

class ZerodhaClientTest {

    @Test
    void tryZero() {
        var z = new ZerodhaClient();
        var k = z.login("testUser");
        z.logout(k);
    }

    @Test
    void shouldGetData() throws Exception, KiteException {
        var z = new ZerodhaClient();
        var k = z.login("testUser");
        List<Holding> holdings = k.getHoldings();
        for (var h : holdings) {
            System.out.printf("%s\t%s\t%d\t%s%n", h.tradingSymbol, h.lastPrice, h.quantity, h.averagePrice);
        }
    }

    @ParameterizedTest
    @CsvFileSource(files = "C:\\Users\\ADMIN\\Documents\\zerodha-secret\\users.txt")
    void shouldLogGTTInfo(String userName) {
        ZerodhaClient.printStopLossInfo(userName);
    }
}
package com.vkg.finance.share.stock.client;

import org.junit.jupiter.api.Test;

class ChartInkClientTest {

    @Test
    void shouldGetChartInkData() {
        var chartInk = new ChartInkClient("super-performance-stocks-11");
        var infoList = chartInk.scrap();
        System.out.println(infoList);
    }
}
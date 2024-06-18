package com.vkg.finance.share.stock.client;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

class NSEJSoupClientTest {

    private final NSEJSoupClient unit = new NSEJSoupClient();

    @Test
    void shouldFetchIndex() {
        HashMap<String, String> map = new HashMap<>();
        map.put("index", "NIFTY 50");
        String data = unit.get("/api/equity-stockIndices", map);
        System.out.println(data);
    }

    @Test
    void shouldChartData() {
        HashMap<String, String> map = new HashMap<>();
        map.put("index", "DABUREQN");
        map.put("preopen", "true");
        String data = unit.get("/api/chart-databyindex", map);
        System.out.println(data);
    }

    @Test
    void shouldFetchStockDetails() {
        HashMap<String, String> map = new HashMap<>();
        map.put("symbol", "NIFTYBEES");
        String data = unit.get("/api/quote-equity", map);
        System.out.println(data);
    }

    @Test
    void shouldFetchCorpInfo() {
        HashMap<String, String> map = new HashMap<>();
        map.put("symbol", "HDFCBANK");
        map.put("market", "equities");
        String data = unit.get("/api/top-corp-info", map);
        System.out.println(data);
    }

    @Test
    void shouldFetchEquityMaster() {
        HashMap<String, String> map = new HashMap<>();
        String data = unit.get("/api/equity-master", map);
        System.out.println(data);
    }

    @Test
    void shouldFetchAllIndices() {
        HashMap<String, String> map = new HashMap<>();
        String data = unit.get("/api/allIndices", map);
        System.out.println(data);
    }

}
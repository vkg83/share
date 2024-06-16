package com.vkg.finance.share.stock.client;

import org.jsoup.Connection;

import java.util.Map;

public interface NSEClient {

    String callApi(String relativePath, Connection.Method method, Map<String, String> params);

}

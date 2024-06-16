package com.vkg.finance.share.stock.client;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.net.Proxy;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@EnableRetry
public class NSEJSoupClient implements NSEClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NSEJSoupClient.class);

    private static final String BASE_URL = "https://www.nseindia.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private Map<String, String> cookies;

    private void loadCookies() {
        if (cookies != null) return;
        try {
            cookies = Jsoup.connect(BASE_URL)
                    .userAgent(USER_AGENT)
                    .proxy(Proxy.NO_PROXY)
                    .method(Connection.Method.GET)
                    .execute().cookies();
        } catch (Exception e) {
            throw new RuntimeException("Not able to fetch cookie!!", e);
        }
    }

    @Override
    @Retryable(retryFor = RuntimeException.class)
    public String callApi(String relativePath, Connection.Method method, Map<String, String> params) {
        try {
            loadCookies();
            Connection.Response resp = Jsoup.connect(BASE_URL + relativePath)
                    .data(params)
                    .userAgent(USER_AGENT)
                    .proxy(Proxy.NO_PROXY)
                    .ignoreContentType(true)
                    .cookies(cookies)
                    .method(method)
                    .execute();
            return resp.body();

        } catch (Exception e) {
            cookies = null;
            throw new RuntimeException("Not able to fetch data!!", e);
        }
    }

}

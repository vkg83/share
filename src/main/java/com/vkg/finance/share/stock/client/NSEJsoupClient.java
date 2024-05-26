package com.vkg.finance.share.stock.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vkg.finance.share.stock.model.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NSEJsoupClient implements FundDataProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(NSEJsoupClient.class);

    private static final String BASE_URL = "https://www.nseindia.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
    private static final String DATA_DUMP_PATH = "C:\\Users\\Vishnu Kant Gupta\\Documents\\nse_data";
    private Map<String, String> cookies;

    @Value("${rest.cache.enable}")
    private boolean enableCache;

    private static <T> Function<String, T> createJsonMapper(Class<T> cls) {
        return str -> {
            ObjectMapper om = new ObjectMapper();
            try {
                return om.readValue(str, cls);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };
    }

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

    private <T> T callApi(String relativePath, Connection.Method method, Map<String, String> params, Class<T> cls) {
        String response = loadFromFile(relativePath, method, params);
        if(response == null) {
            LOGGER.debug("Calling API");
            response = callApiInternal(relativePath, method, params);
            saveToFile(relativePath, method,params, response);
        } else {
            LOGGER.debug("Loaded from cached file");
        }

        return createJsonMapper(cls).apply(response);
    }

    private String callApiInternal(String relativePath, Connection.Method method, Map<String, String> params) {
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
            LOGGER.debug("Some issue occurred in call", e);
            throw new RuntimeException("Not able to fetch data!!", e);
        }
    }

    private String getKey(String relativePath, Connection.Method method, Map<String, String> params) {
        return (relativePath + method + params).replaceAll("\\W", "") + ".txt";
    }

    private String loadFromFile(String relativePath, Connection.Method method, Map<String, String> params) {
        if(!enableCache) return null;
        String key = getKey(relativePath, method, params);
        try {
            return new String(Files.readAllBytes(Paths.get(DATA_DUMP_PATH, LocalDate.now().toString(), key)));
        } catch (IOException e) {
            return null;
        }
    }

    private void saveToFile(String relativePath, Connection.Method method, Map<String, String> params, String response) {
        String key = getKey(relativePath, method, params);
        try {
            Files.createDirectories(Paths.get(DATA_DUMP_PATH, LocalDate.now().toString()));
            Files.write(Paths.get(DATA_DUMP_PATH, LocalDate.now().toString(), key), response.getBytes());
        } catch (IOException e) {
            LOGGER.debug("Not able to save data", e);
        }
    }

    @Override
    public List<Fund> getAllFunds(FundType type) {
        AllFund result = callApi("/api/etf", Connection.Method.GET, Collections.emptyMap(), AllFund.class);
        final List<Fund> funds = result.getData();
        funds.forEach(f -> f.setType(type));
        return funds;
    }

    @Override
    public List<FundHistory> getHistory(Fund fund) {
        Map<String, String> params = Collections.singletonMap("symbol", fund.getSymbol());
        final AllFundHistory allFundHistory = callApi("/api/historical/cm/equity", Connection.Method.GET, params, AllFundHistory.class);
        return allFundHistory.getData();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AllFund {

        private List<Fund> data;

        public List<Fund> getData() {
            return data;
        }

        public void setData(List<Fund> data) {
            this.data = data;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AllFundHistory {

        private List<FundHistory> data;

        public List<FundHistory> getData() {
            return data;
        }

        public void setData(List<FundHistory> data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return data.stream()
                    .map(FundHistory::toString).collect(Collectors.joining("\n"));
        }
    }
}

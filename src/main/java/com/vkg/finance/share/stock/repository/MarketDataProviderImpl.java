package com.vkg.finance.share.stock.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vkg.finance.share.stock.client.NSEClient;
import com.vkg.finance.share.stock.config.MarketConfig;
import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundType;
import com.vkg.finance.share.stock.repository.json.*;
import com.vkg.finance.share.stock.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MarketDataProviderImpl implements MarketDataProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataProviderImpl.class);

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final Map<String, String> memoCache = new HashMap<>();
    private final Map<String, List<FundHistory>> historyCache = new HashMap<>();
    private final List<FundInfo> fundInfoCache = new ArrayList<>();

    @Value("${rest.cache.enable}")
    private boolean enableCache;
    @Value("${rest.cache.path}")
    private Path cacheBasePath;
    @Autowired
    private NSEClient nseClient;
    @Autowired
    private MarketConfig marketConfig;

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

    private <T> T callApi(String relativePath, Map<String, String> params, Class<T> cls) {
        final String fileName = getFileName(relativePath, params);
        String response = loadFromFile(fileName);
        if(response == null) {
            LOGGER.info("Calling API {}", fileName);
            response = nseClient.get(relativePath, params);
            saveToFile(fileName, response);
        } else {
            LOGGER.debug("Loaded from cached {} file", fileName);
        }

        return createJsonMapper(cls).apply(response);
    }

    private String getFileName(String relativePath, Map<String, String> params) {
        return (relativePath + "GET" + params).replaceAll("\\W", "") + ".txt";
    }

    private String loadFromFile(String fileName) {
        if(!enableCache) return null;

        if(memoCache.containsKey(fileName)) {
            return memoCache.get(fileName);
        }
        String response = null;
        try {
            int count = FileUtil.refresh(cacheBasePath);
            if(count > 0) {
                LOGGER.info("Removed {} files while refresh", count);
            }
            response = FileUtil.loadFromFile(getCachePathForToday().resolve(fileName));
            memoCache.put(fileName, response);
        } catch (IOException e) {
            LOGGER.debug("Not able to load data from {}", fileName, e);
        }

        return response;
    }

    private void saveToFile(String fileName, String response) {
        try {
            FileUtil.saveToFile(getCachePathForToday().resolve(fileName), response);
        } catch (IOException e) {
            LOGGER.debug("Not able to save data", e);
        }
    }

    private Path getCachePathForToday() {
        return cacheBasePath.resolve(LocalDate.now().toString());
    }

    @Override
    public List<FundInfo> getAllFunds(FundType type) {
        if(fundInfoCache.isEmpty()) {
            AllFund result = callApi("/api/etf", Collections.emptyMap(), AllFund.class);
            final List<FundInfo> fundInfos = result.getData();
            fundInfos.forEach(f -> f.setType(type));
            fundInfoCache.addAll(fundInfos);
        }
        return fundInfoCache;
    }

    private List<FundHistory> getHistory(String symbol) {
        if(historyCache.containsKey(symbol))
            return historyCache.get(symbol);

        Map<String, String> params = new HashMap<>();
        params.put("symbol", symbol);

        final LocalDate today = LocalDate.now();
        LocalDate from = today.with(IsoFields.DAY_OF_QUARTER, 1);
        LocalDate to = today;
        List<FundHistory> historyList = new ArrayList<>();
        for(int i = 0; i < 25; i++) { //25
            params.put("from", from.format(FORMATTER));
            params.put("to", to.format(FORMATTER));
            final AllFundHistory allFundHistory = callApi("/api/historical/cm/equity", params, AllFundHistory.class);
            historyList.addAll(allFundHistory.getData());
            to = from.minusDays(1);
            from = to.with(IsoFields.DAY_OF_QUARTER, 1);
        }

        if(marketConfig.isMarketOpen(today)) {
            FundHistory cur = getCurrentHistory(symbol, today);
            if( historyList.isEmpty() || historyList.get(0).getDate().isBefore(today)) {
                historyList.add(0, cur);
            } else {
                historyList.set(0, cur);
            }
        }

        findFundInfo(symbol).ifPresent(fundInfo -> historyList.forEach(h -> h.adjust(fundInfo)));
        historyCache.put(symbol, historyList);

        return historyList;
    }

    private FundHistory getCurrentHistory(String symbol, LocalDate date) {
        List<FundHistory> result = callApi("/api/etf", Map.of(), EtfCurrent.class).getData();
        var op = result.stream().filter(f->f.getSymbol().equals(symbol)).peek(f-> f.setDate(date)).findAny();
        if(op.isPresent()) {
            return op.get();
        }

        result = callApi("/api/equity-stockIndices", Map.of("index", "NIFTY 50"), StockCurrent.class).getData();
        op = result.stream().filter(f->f.getSymbol().equals(symbol)).peek(f-> f.setDate(date)).findAny();
        if(op.isPresent()) {
            return op.get();
        }
        LOGGER.info("Calling API for current price of {}", symbol);
        String data = nseClient.get("/api/quote-equity", Map.of("symbol", symbol));
        return createJsonMapper(FundDetail.class).apply(data).toFundHistory(symbol, date);
    }

    private Optional<FundInfo> findFundInfo(String symbol) {
        return getAllFunds(FundType.ETF).stream().filter(f -> f.getSymbol().equals(symbol)).findAny();
    }

    @Override
    public List<FundHistory> getHistory(String symbol, LocalDate date, int days) {
        return getHistory(symbol).stream()
                .filter(h -> !h.getDate().isAfter(date)).limit(days).collect(Collectors.toList());
    }

    @Override
    public List<FundHistory> getHistory(String symbol, LocalDate start, LocalDate end) {
        return getHistory(symbol).stream()
                .filter(h->!h.getDate().isBefore(start) && !h.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FundHistory> getHistory(String symbol, LocalDate date) {
        return getHistory(symbol).stream()
                .filter(h->h.getDate().equals(date)).findAny();
    }

    @Override
    public void clearCache() {
        try {
            FileUtil.delete(cacheBasePath);
        } catch (IOException e) {
            throw new RuntimeException("Not able to cleanup cache", e);
        }
    }

}

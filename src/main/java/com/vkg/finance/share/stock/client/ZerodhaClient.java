package com.vkg.finance.share.stock.client;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.GTT;
import com.zerodhatech.models.GTTParams;
import com.zerodhatech.models.Holding;
import com.zerodhatech.models.User;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ZerodhaClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZerodhaClient.class);
    private static final Path SECRET_DIR = Path.of("C:\\Users\\ADMIN\\Documents\\zerodha-secret");
    private static final String REDIRECT_URL = "127.0.0.1";
    private static final By SUBMIT_BUTTON = By.xpath("/html/body/div[1]/div/div[2]/div[1]/div/div/div[2]/form/div[4]/button");
    public static final List<String> etfs = List.of("NIFTYBEES", "JUNIORBEES", "LIQUIDBEES");
    private static final Map<String, String> BSE_TO_NSE = Map.of("GVTD", "GVT&D");
    public static final double STOP_LOSS_PERCENT = 0.92;

    public KiteConnect login(String userName) {
        KiteConnect kiteSdk;
        String apiSecret;
        String password;
        try {
            var lines = Files.readAllLines(SECRET_DIR.resolve(userName+"-API"));
            kiteSdk = new KiteConnect(lines.get(0));
            apiSecret = lines.get(1);
            password = lines.get(2);
        } catch (IOException ex) {
            throw new RuntimeException("API key not found", ex);
        }
        kiteSdk.setUserId(userName);
        kiteSdk.setSessionExpiryHook(() -> LOGGER.info("session expired"));

        try {
            var lines = Files.readAllLines(SECRET_DIR.resolve(userName));
            kiteSdk.setAccessToken(lines.get(0));
            kiteSdk.setPublicToken(lines.get(1));
            kiteSdk.getProfile();
            LOGGER.info("Logged in {} using existing token", userName);
            return kiteSdk;
        } catch (IOException e) {
            LOGGER.info("Access token not exists.");
        } catch (KiteException ex) {
            LOGGER.info("Expired token");
        }
        String requestToken = WebBrowser.execute(driver -> {
            try {
                return loadRequestToken(driver, kiteSdk.getLoginURL(), userName, password);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            User user = kiteSdk.generateSession(requestToken, apiSecret);
            String content = user.accessToken + "\n" + user.publicToken;
            Files.writeString(SECRET_DIR.resolve(userName), content);
            kiteSdk.setAccessToken(user.accessToken);
            kiteSdk.setPublicToken(user.publicToken);
        } catch (Exception | KiteException ex) {
            LOGGER.error("Error in Zerodha login", ex);
        }
        LOGGER.info("Logged into Zerodha using existing token");
        return kiteSdk;
    }

    private String loadRequestToken(WebDriver webDriver, String loginUrl, String userName, String password) throws MalformedURLException {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofMinutes(10));
        webDriver.get(loginUrl);
        WebElement loginField = webDriver.findElement(By.id("userid"));
        WebElement pwdField = webDriver.findElement(By.id("password"));
        WebElement submitButton = webDriver.findElement(SUBMIT_BUTTON);
        loginField.sendKeys(userName);
        pwdField.sendKeys(password);
        submitButton.click();
        wait.until(ExpectedConditions.urlContains(REDIRECT_URL));
        String curUrl = webDriver.getCurrentUrl();
        String requestToken = null;
        if (curUrl != null && curUrl.contains(REDIRECT_URL)) {
            requestToken = extractToken(curUrl);
            LOGGER.info("Found Request Token - {}", requestToken);
        }

        return requestToken;
    }

    private static String extractToken(String curUrl) {
        String[] pairs;
        try {
            URL url = URI.create(curUrl).toURL();
            String query = url.getQuery();
            pairs = query.split("&");
        } catch (Exception e) {
            throw new RuntimeException("Not able to extract parameters fom url", e);
        }
        String prefix = "request_token=";
        var str = Arrays.stream(pairs).filter(s -> s.startsWith(prefix)).findAny().orElse(prefix);
        return str.substring(prefix.length());
    }

    public void logout(KiteConnect kiteConnect) {
        try {
            var userName = kiteConnect.getUserId();
            var resp = kiteConnect.logout();
            LOGGER.info("Logout response {}", resp);
            Files.deleteIfExists(SECRET_DIR.resolve(userName));
        } catch (KiteException | IOException ex) {
            LOGGER.error("Error in Zerodha logout", ex);
        }
    }

    public static List<String> getHoldings(String userName) {
        try {
            var k = new ZerodhaClient().login(userName);
            List<String> holdings = k.getHoldings().stream().map(h -> getTradingSymbol(h.tradingSymbol)).toList();
            LOGGER.info("Holdings: {}", holdings);
            return holdings;
        } catch (Exception | KiteException ex) {
            LOGGER.error("Can't fetch Holdings from Account", ex);
        }
        return List.of();
    }

    private static String getTradingSymbol(String tradingSymbol) {
        return BSE_TO_NSE.getOrDefault(tradingSymbol, tradingSymbol);
    }

    public static void printStopLossInfo(String userName) {
        try {
            var k = new ZerodhaClient().login(userName);
            var holdings = k.getHoldings();
            Map<String, GTT> gttMap = k.getGTTs().stream()
                    .filter(gtt -> gtt.status.equalsIgnoreCase("ACTIVE"))
                    .collect(Collectors.toMap(gtt-> gtt.condition.tradingSymbol, Function.identity()));

            for(Holding h : holdings) {
                String symbol = getTradingSymbol(h.tradingSymbol);
                if(etfs.contains(symbol)) {
                    continue;
                }
                var gtt = gttMap.remove(symbol);
                if (gtt != null) {
                    compareHoldingWithGTT(symbol, h, gtt);
                } else if(h.quantity > 0 || h.t1Quantity > 0) {
                    LOGGER.error("*** Holding without GTT: {}", symbol);
                    placeStopLoss(k, h);
                }
            }
            if(!gttMap.isEmpty()) {
                LOGGER.error("*** GTT without Holding: {}", gttMap.keySet());
                for (var e: gttMap.entrySet()) {
                    var gtt = e.getValue();
                    k.cancelGTT(gtt.id);
                    LOGGER.info("Cancelled GTT {} for {}", gtt.id, e.getKey());
                }
            }
        } catch (Exception | KiteException ex) {
            LOGGER.error("Can't fetch Holdings from Account", ex);
        }
    }

    private static void placeStopLoss(KiteConnect k, Holding h) throws IOException, KiteException {
        GTTParams p = new GTTParams();
        p.triggerType = Constants.SINGLE;
        p.exchange = Constants.EXCHANGE_NSE;
        p.tradingsymbol = getTradingSymbol(h.tradingSymbol);
        p.lastPrice = h.lastPrice;

        GTTParams.GTTOrderParams order1Params = p.new GTTOrderParams();
        order1Params.orderType = Constants.ORDER_TYPE_LIMIT;
        order1Params.product = Constants.PRODUCT_CNC;
        order1Params.transactionType = Constants.TRANSACTION_TYPE_SELL;
        order1Params.quantity = h.quantity + h.t1Quantity;
        double stopLoss =  h.averagePrice * STOP_LOSS_PERCENT;
        var price = Math.ceil(stopLoss);
        order1Params.price = price;

        p.orders = List.of(order1Params);
        p.triggerPrices = List.of(price + 1);

        k.placeGTT(p);
        LOGGER.info("Stop loss GTT order placed for {}", p.tradingsymbol);
    }

    private static void compareHoldingWithGTT(String symbol, Holding h, GTT gtt) {
        GTT.GTTOrder gttOrder = gtt.orders.getFirst();

        int quantity = h.quantity + h.t1Quantity;
        if(quantity != gttOrder.quantity) {
            LOGGER.error("{} - GTT order {} quantity mismatched {} holding quantity", symbol, gttOrder.quantity, quantity);
        }
        double stopLoss =  h.averagePrice * STOP_LOSS_PERCENT;
        if (stopLoss - gttOrder.price > 0.5) {
            LOGGER.error("{} - GTT stop loss {} is below ideal Stop loss {}", symbol, gttOrder.price, stopLoss);
        } else if (gtt.condition.triggerValues.getFirst() - gttOrder.price < 0.5) {
            LOGGER.error("{} - Gap expected between trigger {} and limit {}", symbol, gtt.condition.triggerValues.getFirst(), gttOrder.price);
        } else {
            double actualStopLoss = (gttOrder.price - h.averagePrice) / h.averagePrice * 100;
            LOGGER.info("{} - Stop loss {}", String.format("%-10s", symbol), String.format("%6.2f%%", actualStopLoss));
        }
    }
}

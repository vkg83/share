package com.vkg.finance.share.stock.client;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.GTT;
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
            URL url = new URL(curUrl);
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
            List<String> holdings = k.getHoldings().stream().map(h -> h.tradingSymbol).toList();
            LOGGER.info("Holdings: {}", holdings);
            return holdings;
        } catch (Exception | KiteException ex) {
            LOGGER.error("Can't fetch Holdings from Account", ex);
        }
        return List.of();
    }
    public static void printStopLossInfo(String userName) {
        try {
            var k = new ZerodhaClient().login(userName);
            var holdings = k.getHoldings();
            Map<String, GTT> gttMap = k.getGTTs().stream().filter(gtt -> gtt.status.equalsIgnoreCase("ACTIVE")).collect(Collectors.toMap(gtt-> gtt.condition.tradingSymbol, Function.identity()));

            for(Holding h : holdings) {
                if(etfs.contains(h.tradingSymbol)) {
                    continue;
                }
                var gtt = gttMap.remove(h.tradingSymbol);
                if (gtt != null) {
                    GTT.GTTOrder gttOrder = gtt.orders.get(0);

                    int quantity = h.quantity + h.t1Quantity;
                    if(quantity != gttOrder.quantity) {
                        LOGGER.error("{} - GTT order {} quantity mismatched {} holding quantity", h.tradingSymbol, gttOrder.quantity,  quantity);
                    }
                    int stopLoss = (int) (h.averagePrice * 0.92);
                    int gttPrice = (int) gttOrder.price;
                    if (stopLoss - gttPrice > 1) {
                        LOGGER.error("{} - GTT stop loss {} is below ideal Stop loss {}", h.tradingSymbol, gttPrice, stopLoss);
                    } else if (gtt.condition.triggerValues.get(0) - gttOrder.price < 0.5) {
                        LOGGER.error("{} - Gap expected between trigger {} and limit {}", h.tradingSymbol, gtt.condition.triggerValues.get(0), gttOrder.price);
                    } else {
                        var v = String.format("%.2f%%", (gttOrder.price - h.averagePrice) / h.averagePrice * 100);
                        LOGGER.info("{} - Stop loss {}", h.tradingSymbol, v);
                    }
                } else {
                    LOGGER.error("Holding without GTT: {}", h.tradingSymbol);
                }
            }
            if(!gttMap.isEmpty()) {
                LOGGER.error("GTT without Holding: {}", gttMap.keySet());
            }
        } catch (Exception | KiteException ex) {
            LOGGER.error("Can't fetch Holdings from Account", ex);
        }
    }
}

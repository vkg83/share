package com.vkg.finance.share.stock.client;

import java.util.Map;

public interface NSEClient {

    String get(String relativePath, Map<String, String> params);

}

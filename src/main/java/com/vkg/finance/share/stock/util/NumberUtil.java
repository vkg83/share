package com.vkg.finance.share.stock.util;

import java.math.BigDecimal;
import java.util.List;

public class NumberUtil {

    private static final List<String> NON_NUM = List.of("N/A", "-");

    public static BigDecimal getBigDecimal(String value) {
        if (value == null || value.isBlank() || NON_NUM.contains(value)) {
            return null;
        }
        value = value.endsWith("%") ? value.substring(0, value.length() - 1) : value;
        return new BigDecimal(value.replaceAll(",", ""));
    }

    public static Integer parseInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean isNotInteger(String value) {
        return parseInt(value) == null;
    }

}

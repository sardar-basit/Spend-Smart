package com.example.spendsmart.util;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public final class CurrencyFormatter {
    private static final Locale LOCALE = Locale.US;
    private static final AtomicReference<String> ACTIVE_CODE = new AtomicReference<>("PKR");

    private CurrencyFormatter() {
    }

    public static void setActiveCurrency(String code) {
        ACTIVE_CODE.set("USD".equalsIgnoreCase(code) ? "USD" : "PKR");
    }

    public static String activeCurrencyCode() {
        return ACTIVE_CODE.get();
    }

    public static String format(double amount) {
        return format(amount, activeCurrencyCode());
    }

    public static String format(double amount, String currencyCode) {
        String code = "USD".equalsIgnoreCase(currencyCode) ? "USD" : "PKR";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(LOCALE);
        formatter.setCurrency(Currency.getInstance(code));
        String raw = formatter.format(amount);
        if ("PKR".equals(code)) {
            String numberPart = raw.replaceAll("[^0-9.,\\-]", "").trim();
            return "Rs. " + numberPart;
        }
        return raw;
    }

    public static String formatSigned(double amount, boolean isIncome) {
        return formatSigned(amount, isIncome, activeCurrencyCode());
    }

    public static String formatSigned(double amount, boolean isIncome, String currencyCode) {
        String prefix = isIncome ? "+" : "−";
        return prefix + format(amount, currencyCode);
    }
}

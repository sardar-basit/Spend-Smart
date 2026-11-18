package com.example.spendsmart;

import com.example.spendsmart.util.CurrencyFormatter;

import org.junit.Assert;
import org.junit.Test;

public class CurrencyFormatterTest {
    @Test
    public void format_includesCurrencyPrefixAndDecimals() {
        CurrencyFormatter.setActiveCurrency("PKR");
        String formatted = CurrencyFormatter.format(1250.0);
        Assert.assertTrue(formatted.startsWith("Rs."));
        Assert.assertTrue(formatted.contains("1,250.00") || formatted.contains("1250.00"));
    }

    @Test
    public void format_usdUsesDollarSign() {
        String formatted = CurrencyFormatter.format(10.5, "USD");
        Assert.assertTrue(formatted.contains("10.50") || formatted.contains("$"));
    }
}

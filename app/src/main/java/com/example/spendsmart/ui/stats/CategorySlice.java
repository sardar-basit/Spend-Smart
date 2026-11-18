package com.example.spendsmart.ui.stats;

public class CategorySlice {
    public final String category;
    public final double amount;
    public final float percentage;
    public final int color;

    public CategorySlice(String category, double amount, float percentage, int color) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
        this.color = color;
    }
}

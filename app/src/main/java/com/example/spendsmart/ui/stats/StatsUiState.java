package com.example.spendsmart.ui.stats;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class StatsUiState {
    public final YearMonth selectedMonth;
    public final List<CategorySlice> slices;
    public final double totalExpense;
    public final double totalIncome;
    public final double previousExpense;
    public final double previousIncome;
    public final List<String> insights;
    public final List<BarChartView.Bar> weeklyBars;

    public StatsUiState(YearMonth selectedMonth, List<CategorySlice> slices,
                        double totalExpense, double totalIncome,
                        double previousExpense, double previousIncome,
                        List<String> insights,
                        List<BarChartView.Bar> weeklyBars) {
        this.selectedMonth = selectedMonth;
        this.slices = slices != null ? slices : new ArrayList<>();
        this.totalExpense = totalExpense;
        this.totalIncome = totalIncome;
        this.previousExpense = previousExpense;
        this.previousIncome = previousIncome;
        this.insights = insights != null ? insights : new ArrayList<>();
        this.weeklyBars = weeklyBars != null ? weeklyBars : new ArrayList<>();
    }
}

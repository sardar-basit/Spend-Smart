package com.example.spendsmart.ui.dashboard;

import com.example.spendsmart.data.local.entity.Transaction;

import java.util.ArrayList;
import java.util.List;

public class DashboardUiState {
    public final double balance;
    public final double totalIncome;
    public final double totalExpense;
    public final double monthlyLimit;
    public final double savingsGoal;
    public final String dailyLimitText;
    public final List<Transaction> recentTransactions;

    public DashboardUiState(double balance, double totalIncome, double totalExpense,
                            double monthlyLimit, double savingsGoal, String dailyLimitText,
                            List<Transaction> recentTransactions) {
        this.balance = balance;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.monthlyLimit = monthlyLimit;
        this.savingsGoal = savingsGoal;
        this.dailyLimitText = dailyLimitText;
        this.recentTransactions = recentTransactions != null
                ? recentTransactions : new ArrayList<>();
    }
}

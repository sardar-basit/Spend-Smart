package com.example.spendsmart.ui.stats;

import com.example.spendsmart.data.local.entity.CategoryBudget;
import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.util.CurrencyFormatter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class InsightBuilder {
    private InsightBuilder() {
    }

    public static List<String> build(YearMonth month,
                                     List<Transaction> current,
                                     List<Transaction> previous,
                                     List<CategoryBudget> categoryBudgets,
                                     double monthlyLimit) {
        List<String> insights = new ArrayList<>();
        double income = 0;
        double expense = 0;
        Map<String, Double> byCategory = new HashMap<>();
        for (Transaction tx : current) {
            if (tx.type == TransactionType.INCOME) {
                income += tx.amount;
            } else {
                expense += tx.amount;
                Double prev = byCategory.get(tx.category);
                byCategory.put(tx.category, (prev == null ? 0.0 : prev) + tx.amount);
            }
        }
        double prevExpense = 0;
        double prevIncome = 0;
        Map<String, Double> prevByCategory = new HashMap<>();
        for (Transaction tx : previous) {
            if (tx.type == TransactionType.INCOME) {
                prevIncome += tx.amount;
            } else {
                prevExpense += tx.amount;
                Double prev = prevByCategory.get(tx.category);
                prevByCategory.put(tx.category, (prev == null ? 0.0 : prev) + tx.amount);
            }
        }

        if (expense == 0 && income == 0) {
            insights.add("No activity this month yet. Add a transaction to unlock insights.");
            return insights;
        }

        if (prevExpense > 0) {
            double delta = ((expense - prevExpense) / prevExpense) * 100.0;
            if (Math.abs(delta) >= 5) {
                insights.add(String.format(Locale.US,
                        "Expenses are %.0f%% %s than last month.",
                        Math.abs(delta), delta > 0 ? "higher" : "lower"));
            } else {
                insights.add("Spending is about the same as last month.");
            }
        }

        String topCategory = null;
        double topAmount = 0;
        for (Map.Entry<String, Double> e : byCategory.entrySet()) {
            if (e.getValue() > topAmount) {
                topAmount = e.getValue();
                topCategory = e.getKey();
            }
        }
        if (topCategory != null && expense > 0) {
            insights.add(String.format(Locale.US,
                    "Top category: %s (%s, %.0f%% of expenses).",
                    topCategory,
                    CurrencyFormatter.format(topAmount),
                    (topAmount / expense) * 100.0));
            Double prevTop = prevByCategory.get(topCategory);
            if (prevTop != null && prevTop > 0) {
                double catDelta = ((topAmount - prevTop) / prevTop) * 100.0;
                if (Math.abs(catDelta) >= 10) {
                    insights.add(String.format(Locale.US,
                            "You spent %.0f%% %s on %s vs last month.",
                            Math.abs(catDelta),
                            catDelta > 0 ? "more" : "less",
                            topCategory));
                }
            }
        }

        if (monthlyLimit > 0 && expense > 0) {
            int day = LocalDate.now().getDayOfMonth();
            int daysInMonth = month.lengthOfMonth();
            if (month.equals(YearMonth.now()) && day > 0) {
                double dailyRate = expense / day;
                double projected = dailyRate * daysInMonth;
                if (projected > monthlyLimit) {
                    int daysLeft = Math.max(1, (int) Math.ceil(
                            (monthlyLimit - expense) / Math.max(dailyRate, 0.01)));
                    if (expense >= monthlyLimit) {
                        insights.add("You are already over your monthly budget.");
                    } else {
                        insights.add(String.format(Locale.US,
                                "At this rate you may hit your budget in about %d day%s.",
                                daysLeft, daysLeft == 1 ? "" : "s"));
                    }
                } else {
                    insights.add(String.format(Locale.US,
                            "On track: projected spend %s vs budget %s.",
                            CurrencyFormatter.format(projected),
                            CurrencyFormatter.format(monthlyLimit)));
                }
            }
        }

        if (categoryBudgets != null) {
            for (CategoryBudget cb : categoryBudgets) {
                Double spent = byCategory.get(cb.category);
                if (spent == null || cb.monthlyLimit <= 0) {
                    continue;
                }
                double pct = (spent / cb.monthlyLimit) * 100.0;
                if (pct >= 100) {
                    insights.add(cb.category + " is over its category budget.");
                } else if (pct >= 80) {
                    insights.add(String.format(Locale.US,
                            "%s is at %.0f%% of its category budget.",
                            cb.category, pct));
                }
            }
        }

        if (income > 0) {
            double saveRate = Math.max(0, income - expense) / income * 100.0;
            insights.add(String.format(Locale.US,
                    "Savings rate this month: %.0f%%.", saveRate));
        }

        if (insights.size() > 4) {
            return insights.subList(0, 4);
        }
        return insights;
    }
}

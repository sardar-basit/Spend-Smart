package com.example.spendsmart.ui.stats;

import android.graphics.Color;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.spendsmart.data.local.entity.CategoryBudget;
import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.data.repository.BudgetRepository;
import com.example.spendsmart.data.repository.CategoryBudgetRepository;
import com.example.spendsmart.data.repository.TransactionRepository;
import com.example.spendsmart.util.DateUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsViewModel extends ViewModel {
    private final TransactionRepository repository;
    private final MediatorLiveData<StatsUiState> uiState = new MediatorLiveData<>();
    private final MutableLiveData<YearMonth> selectedMonth = new MutableLiveData<>(YearMonth.now());

    private LiveData<List<Transaction>> currentMonthSource;
    private LiveData<List<Transaction>> previousMonthSource;
    private List<Transaction> current = new ArrayList<>();
    private List<Transaction> previous = new ArrayList<>();
    private List<CategoryBudget> categoryBudgets = new ArrayList<>();
    private double monthlyLimit;

    public StatsViewModel(TransactionRepository repository,
                          BudgetRepository budgetRepository,
                          CategoryBudgetRepository categoryBudgetRepository) {
        this.repository = repository;
        selectedMonth.observeForever(this::reload);
        uiState.addSource(budgetRepository.observeBudget(), budget -> {
            monthlyLimit = budget != null ? budget.monthlyLimit : 0;
            YearMonth month = selectedMonth.getValue();
            if (month != null) {
                emit(month);
            }
        });
        uiState.addSource(categoryBudgetRepository.observeAll(), list -> {
            categoryBudgets = list != null ? list : new ArrayList<>();
            YearMonth month = selectedMonth.getValue();
            if (month != null) {
                emit(month);
            }
        });
        reload(YearMonth.now());
    }

    public LiveData<StatsUiState> getUiState() {
        return uiState;
    }

    public void previousMonth() {
        YearMonth currentMonth = selectedMonth.getValue();
        if (currentMonth != null) {
            selectedMonth.setValue(currentMonth.minusMonths(1));
        }
    }

    public void nextMonth() {
        YearMonth currentMonth = selectedMonth.getValue();
        if (currentMonth == null) {
            return;
        }
        YearMonth next = currentMonth.plusMonths(1);
        if (!next.isAfter(YearMonth.now())) {
            selectedMonth.setValue(next);
        }
    }

    private void reload(YearMonth month) {
        if (month == null) {
            return;
        }
        if (currentMonthSource != null) {
            uiState.removeSource(currentMonthSource);
        }
        if (previousMonthSource != null) {
            uiState.removeSource(previousMonthSource);
        }
        YearMonth prev = month.minusMonths(1);
        currentMonthSource = repository.getTransactionsByMonth(
                DateUtils.startOfMonth(month), DateUtils.endOfMonth(month));
        previousMonthSource = repository.getTransactionsByMonth(
                DateUtils.startOfMonth(prev), DateUtils.endOfMonth(prev));

        uiState.addSource(currentMonthSource, list -> {
            current = list != null ? list : new ArrayList<>();
            emit(month);
        });
        uiState.addSource(previousMonthSource, list -> {
            previous = list != null ? list : new ArrayList<>();
            emit(month);
        });
    }

    private void emit(YearMonth month) {
        double totalExpense = 0;
        double totalIncome = 0;
        Map<String, Double> byCategory = new HashMap<>();
        for (Transaction tx : current) {
            if (tx.type == TransactionType.EXPENSE) {
                totalExpense += tx.amount;
                Double prevAmt = byCategory.get(tx.category);
                byCategory.put(tx.category, (prevAmt == null ? 0.0 : prevAmt) + tx.amount);
            } else {
                totalIncome += tx.amount;
            }
        }
        double previousExpense = 0;
        double previousIncome = 0;
        for (Transaction tx : previous) {
            if (tx.type == TransactionType.EXPENSE) {
                previousExpense += tx.amount;
            } else {
                previousIncome += tx.amount;
            }
        }
        List<CategorySlice> slices = new ArrayList<>();
        for (Map.Entry<String, Double> entry : byCategory.entrySet()) {
            float pct = totalExpense > 0 ? (float) (entry.getValue() / totalExpense) : 0f;
            slices.add(new CategorySlice(
                    entry.getKey(), entry.getValue(), pct, categoryColor(entry.getKey())));
        }
        slices.sort((a, b) -> Double.compare(b.amount, a.amount));
        List<String> insights = InsightBuilder.build(
                month, current, previous, categoryBudgets, monthlyLimit);
        uiState.setValue(new StatsUiState(
                month, slices, totalExpense, totalIncome, previousExpense, previousIncome,
                insights, buildWeeklyBars(month, current)));
    }

    private List<BarChartView.Bar> buildWeeklyBars(YearMonth month, List<Transaction> txs) {
        float[] weeks = new float[5];
        for (Transaction tx : txs) {
            if (tx.type != TransactionType.EXPENSE) {
                continue;
            }
            LocalDate date = DateUtils.millisToLocalDate(tx.date);
            int weekIndex = Math.min(4, (date.getDayOfMonth() - 1) / 7);
            weeks[weekIndex] += (float) tx.amount;
        }
        List<BarChartView.Bar> bars = new ArrayList<>();
        int[] colors = {
                Color.parseColor("#4DB6AC"),
                Color.parseColor("#26A69A"),
                Color.parseColor("#00897B"),
                Color.parseColor("#00695C"),
                Color.parseColor("#004D40")
        };
        for (int i = 0; i < weeks.length; i++) {
            bars.add(new BarChartView.Bar("W" + (i + 1), weeks[i], colors[i]));
        }
        return bars;
    }

    public static int categoryColor(String category) {
        switch (category) {
            case "Food":
                return Color.parseColor("#E57373");
            case "Rent":
                return Color.parseColor("#64B5F6");
            case "Transport":
                return Color.parseColor("#FFB74D");
            case "Entertainment":
                return Color.parseColor("#BA68C8");
            case "Books":
                return Color.parseColor("#7986CB");
            case "Fees":
                return Color.parseColor("#4DB6AC");
            case "Internet/Mobile Load":
                return Color.parseColor("#F06292");
            case "Utilities":
                return Color.parseColor("#4DB6AC");
            case "Shopping":
                return Color.parseColor("#F06292");
            case "Salary":
            case "Pocket Money":
                return Color.parseColor("#81C784");
            case "Allowance":
                return Color.parseColor("#AED581");
            case "Scholarship":
                return Color.parseColor("#66BB6A");
            case "Part-time":
                return Color.parseColor("#9CCC65");
            default:
                return Color.parseColor("#90A4AE");
        }
    }
}

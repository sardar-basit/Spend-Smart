package com.example.spendsmart.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.spendsmart.data.local.entity.Budget;
import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.data.preferences.PreferencesManager;
import com.example.spendsmart.data.repository.BudgetRepository;
import com.example.spendsmart.data.repository.TransactionRepository;
import com.example.spendsmart.util.CurrencyFormatter;
import com.example.spendsmart.util.DateUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardViewModel extends ViewModel {
    private final MediatorLiveData<DashboardUiState> uiState = new MediatorLiveData<>();
    private final PreferencesManager preferencesManager;
    private List<Transaction> allTx = new ArrayList<>();
    private List<Transaction> monthTx = new ArrayList<>();
    private Budget budget;

    public DashboardViewModel(TransactionRepository transactionRepository,
                              BudgetRepository budgetRepository,
                              PreferencesManager preferencesManager) {
        this.preferencesManager = preferencesManager;
        YearMonth thisMonth = YearMonth.now();
        long start = DateUtils.startOfMonth(thisMonth);
        long end = DateUtils.endOfMonth(thisMonth);

        LiveData<List<Transaction>> allLive = transactionRepository.getAllTransactions();
        LiveData<List<Transaction>> monthLive =
                transactionRepository.getTransactionsByMonth(start, end);
        LiveData<Budget> budgetLive = budgetRepository.observeBudget();

        uiState.addSource(allLive, list -> {
            allTx = list != null ? list : new ArrayList<>();
            emit();
        });
        uiState.addSource(monthLive, list -> {
            monthTx = list != null ? list : new ArrayList<>();
            emit();
        });
        uiState.addSource(budgetLive, b -> {
            budget = b;
            emit();
        });
    }

    public LiveData<DashboardUiState> getUiState() {
        return uiState;
    }

    private void emit() {
        double monthIncome = 0;
        double monthExpense = 0;
        for (Transaction tx : monthTx) {
            if (tx.type == TransactionType.INCOME) {
                monthIncome += tx.amount;
            } else {
                monthExpense += tx.amount;
            }
        }
        double allIncome = 0;
        double allExpense = 0;
        for (Transaction tx : allTx) {
            if (tx.type == TransactionType.INCOME) {
                allIncome += tx.amount;
            } else {
                allExpense += tx.amount;
            }
        }
        List<Transaction> recent = new ArrayList<>();
        for (int i = 0; i < Math.min(5, allTx.size()); i++) {
            recent.add(allTx.get(i));
        }
        double monthlyLimit = budget != null ? budget.monthlyLimit : 0.0;
        String daily;
        if (monthlyLimit <= 0) {
            daily = "Set a monthly budget to unlock your daily spend limit.";
        } else {
            int daysLeft = Math.max(1,
                    YearMonth.now().lengthOfMonth() - LocalDate.now().getDayOfMonth() + 1);
            double remaining = Math.max(0, monthlyLimit - monthExpense);
            daily = String.format(Locale.US,
                    "You can spend %s per day to stay on budget (%d days left).",
                    CurrencyFormatter.format(remaining / daysLeft), daysLeft);
            if (preferencesManager.isSemesterMode()) {
                daily = "Semester mode · " + daily;
            }
        }
        uiState.setValue(new DashboardUiState(
                allIncome - allExpense,
                monthIncome,
                monthExpense,
                monthlyLimit,
                budget != null ? budget.savingsGoal : 0.0,
                daily,
                recent
        ));
    }
}

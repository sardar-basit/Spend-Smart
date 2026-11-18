package com.example.spendsmart.ui.budget;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.spendsmart.data.local.entity.Budget;
import com.example.spendsmart.data.local.entity.CategoryBudget;
import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.data.preferences.PreferencesManager;
import com.example.spendsmart.data.repository.BudgetRepository;
import com.example.spendsmart.data.repository.CategoryBudgetRepository;
import com.example.spendsmart.data.repository.CategoryRepository;
import com.example.spendsmart.data.repository.TransactionRepository;
import com.example.spendsmart.util.CurrencyFormatter;
import com.example.spendsmart.util.DateUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetViewModel extends ViewModel {
    private final BudgetRepository budgetRepository;
    private final CategoryBudgetRepository categoryBudgetRepository;
    private final CategoryRepository categoryRepository;
    private final PreferencesManager preferencesManager;
    private final MediatorLiveData<String> dailyLimitText = new MediatorLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    private Budget budget;
    private List<Transaction> monthTx = new ArrayList<>();

    public BudgetViewModel(BudgetRepository budgetRepository,
                           CategoryBudgetRepository categoryBudgetRepository,
                           CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository,
                           PreferencesManager preferencesManager) {
        this.budgetRepository = budgetRepository;
        this.categoryBudgetRepository = categoryBudgetRepository;
        this.categoryRepository = categoryRepository;
        this.preferencesManager = preferencesManager;

        YearMonth month = YearMonth.now();
        LiveData<List<Transaction>> monthLive = transactionRepository.getTransactionsByMonth(
                DateUtils.startOfMonth(month), DateUtils.endOfMonth(month));
        dailyLimitText.addSource(budgetRepository.observeBudget(), b -> {
            budget = b;
            recomputeDaily();
        });
        dailyLimitText.addSource(monthLive, list -> {
            monthTx = list != null ? list : new ArrayList<>();
            recomputeDaily();
        });
    }

    public LiveData<Budget> getBudget() {
        return budgetRepository.observeBudget();
    }

    public LiveData<List<CategoryBudget>> getCategoryBudgets() {
        return categoryBudgetRepository.observeAll();
    }

    public LiveData<List<String>> getExpenseCategories() {
        return categoryRepository.observeCategories(TransactionType.EXPENSE);
    }

    public LiveData<String> getDailyLimitText() {
        return dailyLimitText;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getSemesterMode() {
        return preferencesManager.getSemesterModeLive();
    }

    public void setSemesterMode(boolean enabled) {
        preferencesManager.setSemesterMode(enabled);
    }

    public void saveBudget(String limitText, String goalText) {
        budgetRepository.saveBudget(parse(limitText), parse(goalText),
                () -> message.postValue("Budget saved"));
    }

    public void saveCategoryBudget(String category, String limitText) {
        double limit = parse(limitText);
        if (category == null || limit <= 0) {
            message.postValue("Enter a valid category limit");
            return;
        }
        categoryBudgetRepository.save(category, limit,
                () -> message.postValue("Category budget saved"));
    }

    public void deleteCategoryBudget(CategoryBudget budget) {
        categoryBudgetRepository.delete(budget);
        message.postValue("Category budget removed");
    }

    private void recomputeDaily() {
        double limit = budget != null ? budget.monthlyLimit : 0;
        if (limit <= 0) {
            dailyLimitText.setValue("Set a monthly budget to see your daily spend limit.");
            return;
        }
        double spent = 0;
        for (Transaction tx : monthTx) {
            if (tx.type == TransactionType.EXPENSE) {
                spent += tx.amount;
            }
        }
        int daysLeft = Math.max(1,
                YearMonth.now().lengthOfMonth() - LocalDate.now().getDayOfMonth() + 1);
        double remaining = Math.max(0, limit - spent);
        double perDay = remaining / daysLeft;
        dailyLimitText.setValue(String.format(Locale.US,
                "You can spend %s per day to stay on budget (%d days left).",
                CurrencyFormatter.format(perDay), daysLeft));
    }

    private static double parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}

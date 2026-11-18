package com.example.spendsmart.ui.settings;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.spendsmart.data.local.entity.Budget;
import com.example.spendsmart.data.local.entity.CategoryBudget;
import com.example.spendsmart.data.local.entity.CustomCategory;
import com.example.spendsmart.data.local.entity.RecurrenceFrequency;
import com.example.spendsmart.data.local.entity.RecurringTransaction;
import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.data.preferences.PreferencesManager;
import com.example.spendsmart.data.preferences.ThemeMode;
import com.example.spendsmart.data.repository.AuthRepository;
import com.example.spendsmart.data.repository.BudgetRepository;
import com.example.spendsmart.data.repository.CategoryBudgetRepository;
import com.example.spendsmart.data.repository.CategoryRepository;
import com.example.spendsmart.data.repository.RecurringTransactionRepository;
import com.example.spendsmart.data.repository.TransactionRepository;
import com.example.spendsmart.domain.model.AuthResult;
import com.example.spendsmart.domain.model.AuthState;
import com.example.spendsmart.domain.model.Categories;
import com.example.spendsmart.util.CsvExporter;
import com.example.spendsmart.util.DateUtils;
import com.example.spendsmart.util.PdfExporter;

import java.io.File;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class SettingsViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryBudgetRepository categoryBudgetRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final PreferencesManager preferencesManager;
    private final ExecutorService executor;

    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> exportCsv = new MutableLiveData<>();
    private final MutableLiveData<File> exportPdf = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> profileResult = new MutableLiveData<>();

    public SettingsViewModel(AuthRepository authRepository,
                             BudgetRepository budgetRepository,
                             CategoryRepository categoryRepository,
                             CategoryBudgetRepository categoryBudgetRepository,
                             RecurringTransactionRepository recurringTransactionRepository,
                             TransactionRepository transactionRepository,
                             PreferencesManager preferencesManager,
                             ExecutorService executor) {
        this.authRepository = authRepository;
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.categoryBudgetRepository = categoryBudgetRepository;
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.preferencesManager = preferencesManager;
        this.executor = executor;
    }

    public LiveData<AuthState> getAuthState() {
        return authRepository.observeAuthState();
    }

    public LiveData<Budget> getBudget() {
        return budgetRepository.observeBudget();
    }

    public LiveData<List<CustomCategory>> getCustomCategories() {
        return categoryRepository.observeCustomCategories();
    }

    public LiveData<List<String>> getExpenseCategories() {
        return categoryRepository.observeCategories(TransactionType.EXPENSE);
    }

    public LiveData<List<CategoryBudget>> getCategoryBudgets() {
        return categoryBudgetRepository.observeAll();
    }

    public LiveData<List<RecurringTransaction>> getRecurring() {
        return recurringTransactionRepository.observeAll();
    }

    public LiveData<String> getCurrency() {
        return preferencesManager.getCurrencyCodeLive();
    }

    public LiveData<ThemeMode> getThemeMode() {
        return preferencesManager.getThemeModeLive();
    }

    public LiveData<Boolean> getBudgetAlerts() {
        return preferencesManager.getBudgetAlertsEnabledLive();
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getExportCsv() {
        return exportCsv;
    }

    public LiveData<File> getExportPdf() {
        return exportPdf;
    }

    public void saveProfile(String name) {
        MutableLiveData<AuthResult> result = new MutableLiveData<>();
        result.observeForever(r -> {
            if (r == null) {
                return;
            }
            if (r.success) {
                message.postValue("Profile updated");
                error.postValue(null);
            } else {
                error.postValue(r.errorMessage);
                message.postValue(null);
            }
            profileResult.postValue(r);
        });
        authRepository.updateName(name, result);
    }

    public void saveBudget(String limitText, String goalText) {
        double limit = parseDouble(limitText);
        double goal = parseDouble(goalText);
        budgetRepository.saveBudget(limit, goal, () -> message.postValue("Budget saved"));
        error.postValue(null);
    }

    public void saveCategoryBudget(String category, String limitText) {
        double limit = parseDouble(limitText);
        if (category == null || category.trim().isEmpty() || limit <= 0) {
            error.postValue("Enter a valid category limit");
            return;
        }
        categoryBudgetRepository.save(category, limit,
                () -> message.postValue("Category budget saved"));
        error.postValue(null);
    }

    public void deleteCategoryBudget(CategoryBudget budget) {
        categoryBudgetRepository.delete(budget);
        message.postValue("Category budget removed");
    }

    public void addRecurring(String amountText, String category, boolean income,
                             boolean weekly, String note) {
        double amount = parseDouble(amountText);
        if (amount <= 0 || category == null || category.trim().isEmpty()) {
            error.postValue("Enter amount and category for recurring");
            return;
        }
        TransactionType type = income ? TransactionType.INCOME : TransactionType.EXPENSE;
        RecurrenceFrequency freq = weekly ? RecurrenceFrequency.WEEKLY : RecurrenceFrequency.MONTHLY;
        recurringTransactionRepository.add(
                amount,
                type,
                category,
                note,
                freq,
                DateUtils.localDateToMillis(java.time.LocalDate.now()),
                () -> message.postValue("Recurring transaction added")
        );
        error.postValue(null);
    }

    public void deleteRecurring(RecurringTransaction item) {
        recurringTransactionRepository.delete(item);
        message.postValue("Recurring removed");
    }

    public void setCurrency(String code) {
        preferencesManager.setCurrencyCode(code);
    }

    public void setThemeMode(ThemeMode mode) {
        preferencesManager.setThemeMode(mode);
    }

    public void setBudgetAlerts(boolean enabled) {
        preferencesManager.setBudgetAlertsEnabled(enabled);
    }

    public boolean isAppLockEnabled() {
        return preferencesManager.isAppLockEnabled();
    }

    public void setAppLockEnabled(boolean enabled) {
        if (!enabled) {
            preferencesManager.clearPin();
            message.postValue("App lock disabled");
            return;
        }
        preferencesManager.setAppLockEnabled(true);
    }

    public void setPin(String pin, String confirm) {
        if (pin == null || pin.length() < 4) {
            error.postValue("PIN must be at least 4 digits");
            return;
        }
        if (!pin.equals(confirm)) {
            error.postValue("PINs do not match");
            return;
        }
        preferencesManager.setPin(pin);
        message.postValue("PIN set — app lock enabled");
        error.postValue(null);
    }

    public void addCategory(String name, TransactionType type) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        result.observeForever(ok -> {
            if (Boolean.TRUE.equals(ok)) {
                message.postValue("Category added");
                error.postValue(null);
            } else {
                error.postValue("Could not add category (blank or duplicate)");
                message.postValue(null);
            }
        });
        categoryRepository.addCustomCategory(name, type, result);
    }

    public void deleteCategory(CustomCategory category) {
        categoryRepository.deleteCustomCategory(category);
    }

    public void changePassword(String current, String neu) {
        MutableLiveData<AuthResult> result = new MutableLiveData<>();
        result.observeForever(r -> {
            if (r == null) {
                return;
            }
            if (r.success) {
                message.postValue("Password changed");
                error.postValue(null);
            } else {
                error.postValue(r.errorMessage);
                message.postValue(null);
            }
        });
        authRepository.changePassword(current, neu, result);
    }

    public void exportCsv() {
        executor.execute(() -> {
            List<Transaction> txs = transactionRepository.getAllTransactionsSync();
            exportCsv.postValue(CsvExporter.toCsv(txs));
            message.postValue("CSV ready to share");
        });
    }

    public void exportPdf(Context context, String userName) {
        executor.execute(() -> {
            try {
                YearMonth month = YearMonth.now();
                List<Transaction> txs = transactionRepository.getTransactionsByMonthSync(month);
                Budget budget = budgetRepository.getBudgetSync();
                List<CategoryBudget> categoryBudgets = categoryBudgetRepository.getAllSync();
                String name = userName == null || userName.trim().isEmpty() ? "Student" : userName;
                File file = new File(context.getCacheDir(), "spendsmart_report.pdf");
                PdfExporter.exportMonthlyReport(file, name, month, txs, budget, categoryBudgets);
                exportPdf.postValue(file);
                message.postValue("PDF report ready to share");
            } catch (Exception e) {
                error.postValue("PDF export failed");
            }
        });
    }

    public void logout() {
        authRepository.logout();
    }

    public void clearFeedback() {
        message.setValue(null);
        error.setValue(null);
    }

    public void consumeExport() {
        exportCsv.setValue(null);
    }

    public void consumePdfExport() {
        exportPdf.setValue(null);
    }

    public List<String> defaultExpenseCategories() {
        return new ArrayList<>(Categories.EXPENSE);
    }

    private static double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static String formatNumber(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}

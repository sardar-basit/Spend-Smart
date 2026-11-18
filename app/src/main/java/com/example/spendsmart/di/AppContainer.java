package com.example.spendsmart.di;

import android.content.Context;

import com.example.spendsmart.data.local.AppDatabase;
import com.example.spendsmart.data.preferences.PreferencesManager;
import com.example.spendsmart.data.repository.AuthRepository;
import com.example.spendsmart.data.repository.BudgetRepository;
import com.example.spendsmart.data.repository.CategoryBudgetRepository;
import com.example.spendsmart.data.repository.CategoryRepository;
import com.example.spendsmart.data.repository.RecurringTransactionRepository;
import com.example.spendsmart.data.repository.SavingsGoalRepository;
import com.example.spendsmart.data.repository.SplitBillRepository;
import com.example.spendsmart.data.repository.TransactionRepository;
import com.example.spendsmart.data.session.SessionManager;
import com.example.spendsmart.util.BudgetNotifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppContainer {
    public final ExecutorService executor;
    public final SessionManager sessionManager;
    public final PreferencesManager preferencesManager;
    public final BudgetNotifier budgetNotifier;
    public final AuthRepository authRepository;
    public final TransactionRepository transactionRepository;
    public final BudgetRepository budgetRepository;
    public final CategoryRepository categoryRepository;
    public final CategoryBudgetRepository categoryBudgetRepository;
    public final RecurringTransactionRepository recurringTransactionRepository;
    public final SavingsGoalRepository savingsGoalRepository;
    public final SplitBillRepository splitBillRepository;

    public AppContainer(Context context) {
        Context app = context.getApplicationContext();
        executor = Executors.newSingleThreadExecutor();
        AppDatabase db = AppDatabase.getInstance(app);
        sessionManager = new SessionManager(app);
        preferencesManager = new PreferencesManager(app);
        budgetNotifier = new BudgetNotifier(app);
        budgetNotifier.ensureChannel();

        authRepository = new AuthRepository(db.userDao(), sessionManager, executor);
        budgetRepository = new BudgetRepository(db.budgetDao(), sessionManager, executor);
        categoryRepository = new CategoryRepository(db.customCategoryDao(), sessionManager, executor);
        categoryBudgetRepository = new CategoryBudgetRepository(
                db.categoryBudgetDao(), sessionManager, executor);
        recurringTransactionRepository = new RecurringTransactionRepository(
                db.recurringTransactionDao(),
                db.transactionDao(),
                sessionManager,
                executor
        );
        savingsGoalRepository = new SavingsGoalRepository(
                db.savingsGoalDao(), sessionManager, executor);
        splitBillRepository = new SplitBillRepository(
                db.splitBillDao(), sessionManager, executor);
        transactionRepository = new TransactionRepository(
                db.transactionDao(),
                db.budgetDao(),
                db.categoryBudgetDao(),
                sessionManager,
                preferencesManager,
                budgetNotifier,
                executor
        );
    }
}

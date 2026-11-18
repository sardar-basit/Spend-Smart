package com.example.spendsmart.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.spendsmart.di.AppContainer;
import com.example.spendsmart.ui.addtransaction.AddTransactionViewModel;
import com.example.spendsmart.ui.auth.AuthViewModel;
import com.example.spendsmart.ui.budget.BudgetViewModel;
import com.example.spendsmart.ui.dashboard.DashboardViewModel;
import com.example.spendsmart.ui.history.HistoryViewModel;
import com.example.spendsmart.ui.savings.SavingsViewModel;
import com.example.spendsmart.ui.settings.SettingsViewModel;
import com.example.spendsmart.ui.split.SplitBillsViewModel;
import com.example.spendsmart.ui.stats.StatsViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final AppContainer container;
    private final Integer transactionId;

    public ViewModelFactory(AppContainer container) {
        this(container, null);
    }

    public ViewModelFactory(AppContainer container, Integer transactionId) {
        this.container = container;
        this.transactionId = transactionId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(container.authRepository);
        }
        if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
            return (T) new DashboardViewModel(
                    container.transactionRepository,
                    container.budgetRepository,
                    container.preferencesManager);
        }
        if (modelClass.isAssignableFrom(HistoryViewModel.class)) {
            return (T) new HistoryViewModel(
                    container.transactionRepository, container.categoryRepository);
        }
        if (modelClass.isAssignableFrom(StatsViewModel.class)) {
            return (T) new StatsViewModel(
                    container.transactionRepository,
                    container.budgetRepository,
                    container.categoryBudgetRepository);
        }
        if (modelClass.isAssignableFrom(BudgetViewModel.class)) {
            return (T) new BudgetViewModel(
                    container.budgetRepository,
                    container.categoryBudgetRepository,
                    container.categoryRepository,
                    container.transactionRepository,
                    container.preferencesManager);
        }
        if (modelClass.isAssignableFrom(SavingsViewModel.class)) {
            return (T) new SavingsViewModel(container.savingsGoalRepository);
        }
        if (modelClass.isAssignableFrom(SplitBillsViewModel.class)) {
            return (T) new SplitBillsViewModel(container.splitBillRepository);
        }
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(
                    container.authRepository,
                    container.budgetRepository,
                    container.categoryRepository,
                    container.categoryBudgetRepository,
                    container.recurringTransactionRepository,
                    container.transactionRepository,
                    container.preferencesManager,
                    container.executor
            );
        }
        if (modelClass.isAssignableFrom(AddTransactionViewModel.class)) {
            int id = transactionId == null ? -1 : transactionId;
            return (T) new AddTransactionViewModel(
                    container.transactionRepository,
                    container.categoryRepository,
                    container.executor,
                    id
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}

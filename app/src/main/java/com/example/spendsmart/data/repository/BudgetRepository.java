package com.example.spendsmart.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.spendsmart.data.local.dao.BudgetDao;
import com.example.spendsmart.data.local.entity.Budget;
import com.example.spendsmart.data.session.SessionManager;

import java.util.concurrent.ExecutorService;

public class BudgetRepository {
    private final BudgetDao budgetDao;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public BudgetRepository(BudgetDao budgetDao, SessionManager sessionManager,
                            ExecutorService executor) {
        this.budgetDao = budgetDao;
        this.sessionManager = sessionManager;
        this.executor = executor;
    }

    public LiveData<Budget> observeBudget() {
        return Transformations.switchMap(sessionManager.getCurrentUserIdLive(), userId -> {
            if (userId == null) {
                MutableLiveData<Budget> empty = new MutableLiveData<>();
                empty.setValue(null);
                return empty;
            }
            return budgetDao.observeBudget(userId);
        });
    }

    public Budget getBudgetSync() {
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            return null;
        }
        return budgetDao.getBudget(userId);
    }

    public void saveBudget(double monthlyLimit, double savingsGoal, Runnable onDone) {
        executor.execute(() -> {
            Long userId = sessionManager.getUserId();
            if (userId == null) {
                return;
            }
            budgetDao.upsert(new Budget(
                    userId,
                    Math.max(0.0, monthlyLimit),
                    Math.max(0.0, savingsGoal)
            ));
            if (onDone != null) {
                onDone.run();
            }
        });
    }
}

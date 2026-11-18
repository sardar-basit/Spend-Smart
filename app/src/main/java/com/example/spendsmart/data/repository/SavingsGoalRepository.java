package com.example.spendsmart.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.spendsmart.data.local.dao.SavingsGoalDao;
import com.example.spendsmart.data.local.entity.SavingsGoal;
import com.example.spendsmart.data.session.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class SavingsGoalRepository {
    private final SavingsGoalDao dao;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public SavingsGoalRepository(SavingsGoalDao dao, SessionManager sessionManager,
                                 ExecutorService executor) {
        this.dao = dao;
        this.sessionManager = sessionManager;
        this.executor = executor;
    }

    public LiveData<List<SavingsGoal>> observeAll() {
        return Transformations.switchMap(sessionManager.getCurrentUserIdLive(), userId -> {
            if (userId == null) {
                MutableLiveData<List<SavingsGoal>> empty = new MutableLiveData<>();
                empty.setValue(new ArrayList<>());
                return empty;
            }
            return dao.getAll(userId);
        });
    }

    public void add(String title, double target, double saved, Long deadline, Runnable onDone) {
        executor.execute(() -> {
            Long userId = sessionManager.getUserId();
            if (userId == null || title == null || title.trim().isEmpty() || target <= 0) {
                if (onDone != null) {
                    onDone.run();
                }
                return;
            }
            dao.insert(new SavingsGoal(userId, title.trim(), target, Math.max(0, saved), deadline));
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    public void update(SavingsGoal goal) {
        executor.execute(() -> dao.update(goal));
    }

    public void delete(SavingsGoal goal) {
        executor.execute(() -> dao.delete(goal));
    }

    public void addSavedAmount(SavingsGoal goal, double amount) {
        executor.execute(() -> {
            goal.savedAmount = Math.max(0, goal.savedAmount + amount);
            dao.update(goal);
        });
    }
}

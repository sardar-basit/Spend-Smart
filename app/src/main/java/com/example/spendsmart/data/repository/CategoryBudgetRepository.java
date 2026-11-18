package com.example.spendsmart.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.spendsmart.data.local.dao.CategoryBudgetDao;
import com.example.spendsmart.data.local.entity.CategoryBudget;
import com.example.spendsmart.data.session.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class CategoryBudgetRepository {
    private final CategoryBudgetDao dao;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public CategoryBudgetRepository(CategoryBudgetDao dao,
                                    SessionManager sessionManager,
                                    ExecutorService executor) {
        this.dao = dao;
        this.sessionManager = sessionManager;
        this.executor = executor;
    }

    public LiveData<List<CategoryBudget>> observeAll() {
        return Transformations.switchMap(sessionManager.getCurrentUserIdLive(), userId -> {
            if (userId == null) {
                MutableLiveData<List<CategoryBudget>> empty = new MutableLiveData<>();
                empty.setValue(new ArrayList<>());
                return empty;
            }
            return dao.getAll(userId);
        });
    }

    public List<CategoryBudget> getAllSync() {
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            return new ArrayList<>();
        }
        return dao.getAllSync(userId);
    }

    public void save(String category, double monthlyLimit, Runnable onDone) {
        executor.execute(() -> {
            Long userId = sessionManager.getUserId();
            if (userId == null || category == null || category.trim().isEmpty() || monthlyLimit <= 0) {
                if (onDone != null) {
                    onDone.run();
                }
                return;
            }
            String name = category.trim();
            CategoryBudget existing = dao.getByCategory(userId, name);
            if (existing != null) {
                existing.monthlyLimit = monthlyLimit;
                dao.insert(existing);
            } else {
                dao.insert(new CategoryBudget(userId, name, monthlyLimit));
            }
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    public void delete(CategoryBudget budget) {
        executor.execute(() -> dao.delete(budget));
    }
}

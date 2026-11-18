package com.example.spendsmart.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.spendsmart.data.local.dao.CustomCategoryDao;
import com.example.spendsmart.data.local.entity.CustomCategory;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.data.session.SessionManager;
import com.example.spendsmart.domain.model.Categories;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class CategoryRepository {
    private final CustomCategoryDao customCategoryDao;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public CategoryRepository(CustomCategoryDao customCategoryDao,
                              SessionManager sessionManager,
                              ExecutorService executor) {
        this.customCategoryDao = customCategoryDao;
        this.sessionManager = sessionManager;
        this.executor = executor;
    }

    public LiveData<List<String>> observeCategories(TransactionType type) {
        return Transformations.switchMap(sessionManager.getCurrentUserIdLive(), userId -> {
            List<String> defaults = Categories.forType(type);
            if (userId == null) {
                MutableLiveData<List<String>> live = new MutableLiveData<>();
                live.setValue(defaults);
                return live;
            }
            MediatorLiveData<List<String>> mediator = new MediatorLiveData<>();
            mediator.addSource(customCategoryDao.observeByType(userId, type), custom -> {
                LinkedHashSet<String> set = new LinkedHashSet<>(defaults);
                if (custom != null) {
                    for (CustomCategory c : custom) {
                        set.add(c.name);
                    }
                }
                mediator.setValue(new ArrayList<>(set));
            });
            return mediator;
        });
    }

    public LiveData<List<CustomCategory>> observeCustomCategories() {
        return Transformations.switchMap(sessionManager.getCurrentUserIdLive(), userId -> {
            if (userId == null) {
                MutableLiveData<List<CustomCategory>> empty = new MutableLiveData<>();
                empty.setValue(new ArrayList<>());
                return empty;
            }
            return customCategoryDao.observeAll(userId);
        });
    }

    public void addCustomCategory(String name, TransactionType type,
                                  MutableLiveData<Boolean> result) {
        executor.execute(() -> {
            Long userId = sessionManager.getUserId();
            if (userId == null) {
                result.postValue(false);
                return;
            }
            String trimmed = name == null ? "" : name.trim();
            if (trimmed.isEmpty() || Categories.forType(type).contains(trimmed)) {
                result.postValue(false);
                return;
            }
            long id = customCategoryDao.insert(new CustomCategory(userId, trimmed, type));
            result.postValue(id != -1L);
        });
    }

    public void deleteCustomCategory(CustomCategory category) {
        executor.execute(() -> {
            Long userId = sessionManager.getUserId();
            if (userId != null && category.userId == userId) {
                customCategoryDao.delete(category);
            }
        });
    }
}

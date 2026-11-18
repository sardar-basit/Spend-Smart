package com.example.spendsmart.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.spendsmart.data.local.dao.BudgetDao;
import com.example.spendsmart.data.local.dao.CategoryBudgetDao;
import com.example.spendsmart.data.local.dao.TransactionDao;
import com.example.spendsmart.data.local.entity.Budget;
import com.example.spendsmart.data.local.entity.CategoryBudget;
import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.data.preferences.PreferencesManager;
import com.example.spendsmart.data.session.SessionManager;
import com.example.spendsmart.util.BudgetNotifier;
import com.example.spendsmart.util.DateUtils;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class TransactionRepository {
    private final TransactionDao transactionDao;
    private final BudgetDao budgetDao;
    private final CategoryBudgetDao categoryBudgetDao;
    private final SessionManager sessionManager;
    private final PreferencesManager preferencesManager;
    private final BudgetNotifier budgetNotifier;
    private final ExecutorService executor;

    public TransactionRepository(TransactionDao transactionDao,
                                 BudgetDao budgetDao,
                                 CategoryBudgetDao categoryBudgetDao,
                                 SessionManager sessionManager,
                                 PreferencesManager preferencesManager,
                                 BudgetNotifier budgetNotifier,
                                 ExecutorService executor) {
        this.transactionDao = transactionDao;
        this.budgetDao = budgetDao;
        this.categoryBudgetDao = categoryBudgetDao;
        this.sessionManager = sessionManager;
        this.preferencesManager = preferencesManager;
        this.budgetNotifier = budgetNotifier;
        this.executor = executor;
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return Transformations.switchMap(sessionManager.getCurrentUserIdLive(), userId -> {
            if (userId == null) {
                MutableLiveData<List<Transaction>> empty = new MutableLiveData<>();
                empty.setValue(new ArrayList<>());
                return empty;
            }
            return transactionDao.getAllTransactions(userId);
        });
    }

    public LiveData<List<Transaction>> getTransactionsByMonth(long startOfMonth, long endOfMonth) {
        return Transformations.switchMap(sessionManager.getCurrentUserIdLive(), userId -> {
            if (userId == null) {
                MutableLiveData<List<Transaction>> empty = new MutableLiveData<>();
                empty.setValue(new ArrayList<>());
                return empty;
            }
            return transactionDao.getTransactionsByMonth(userId, startOfMonth, endOfMonth);
        });
    }

    public List<Transaction> getAllTransactionsSync() {
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            return new ArrayList<>();
        }
        return transactionDao.getAllTransactionsSync(userId);
    }

    public List<Transaction> getTransactionsByMonthSync(YearMonth month) {
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            return new ArrayList<>();
        }
        return transactionDao.getTransactionsByMonthSync(
                userId, DateUtils.startOfMonth(month), DateUtils.endOfMonth(month));
    }

    public Transaction getTransactionById(int id) {
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            return null;
        }
        return transactionDao.getTransactionById(id, userId);
    }

    public void insert(Transaction transaction, Runnable onDone) {
        executor.execute(() -> {
            long userId = requireUserId();
            transaction.userId = userId;
            transactionDao.insert(transaction);
            checkBudgetAlert(userId, transaction);
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    public void update(Transaction transaction, Runnable onDone) {
        executor.execute(() -> {
            long userId = requireUserId();
            transaction.userId = userId;
            transactionDao.update(transaction);
            checkBudgetAlert(userId, transaction);
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    public void delete(Transaction transaction, Runnable onDone) {
        executor.execute(() -> {
            Long userId = sessionManager.getUserId();
            if (userId != null && transaction.userId == userId) {
                transactionDao.delete(transaction);
            }
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    private void checkBudgetAlert(long userId, Transaction transaction) {
        if (transaction == null || transaction.type != TransactionType.EXPENSE) {
            return;
        }
        if (!preferencesManager.isBudgetAlertsEnabled()) {
            return;
        }
        YearMonth month = YearMonth.now();
        List<Transaction> monthly = transactionDao.getTransactionsByMonthSync(
                userId, DateUtils.startOfMonth(month), DateUtils.endOfMonth(month));
        double spent = 0;
        Map<String, Double> byCategory = new HashMap<>();
        for (Transaction tx : monthly) {
            if (tx.type == TransactionType.EXPENSE) {
                spent += tx.amount;
                Double prev = byCategory.get(tx.category);
                byCategory.put(tx.category, (prev == null ? 0.0 : prev) + tx.amount);
            }
        }
        Budget budget = budgetDao.getBudget(userId);
        if (budget != null && budget.monthlyLimit > 0.0) {
            budgetNotifier.maybeNotify(spent, budget.monthlyLimit);
        }
        List<CategoryBudget> categoryBudgets = categoryBudgetDao.getAllSync(userId);
        for (CategoryBudget cb : categoryBudgets) {
            Double catSpent = byCategory.get(cb.category);
            if (catSpent != null) {
                budgetNotifier.maybeNotifyCategory(cb.category, catSpent, cb.monthlyLimit);
            }
        }
    }

    private long requireUserId() {
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            throw new IllegalStateException("Not logged in");
        }
        return userId;
    }
}

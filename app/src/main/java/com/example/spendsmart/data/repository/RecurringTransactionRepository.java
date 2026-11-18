package com.example.spendsmart.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.spendsmart.data.local.dao.RecurringTransactionDao;
import com.example.spendsmart.data.local.dao.TransactionDao;
import com.example.spendsmart.data.local.entity.RecurrenceFrequency;
import com.example.spendsmart.data.local.entity.RecurringTransaction;
import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.data.session.SessionManager;
import com.example.spendsmart.util.DateUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class RecurringTransactionRepository {
    private final RecurringTransactionDao recurringDao;
    private final TransactionDao transactionDao;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public RecurringTransactionRepository(RecurringTransactionDao recurringDao,
                                          TransactionDao transactionDao,
                                          SessionManager sessionManager,
                                          ExecutorService executor) {
        this.recurringDao = recurringDao;
        this.transactionDao = transactionDao;
        this.sessionManager = sessionManager;
        this.executor = executor;
    }

    public LiveData<List<RecurringTransaction>> observeAll() {
        return Transformations.switchMap(sessionManager.getCurrentUserIdLive(), userId -> {
            if (userId == null) {
                MutableLiveData<List<RecurringTransaction>> empty = new MutableLiveData<>();
                empty.setValue(new ArrayList<>());
                return empty;
            }
            return recurringDao.getAll(userId);
        });
    }

    public void add(double amount, TransactionType type, String category, String note,
                    RecurrenceFrequency frequency, long firstRunAt, Runnable onDone) {
        executor.execute(() -> {
            Long userId = sessionManager.getUserId();
            if (userId == null || amount <= 0 || category == null || category.trim().isEmpty()) {
                if (onDone != null) {
                    onDone.run();
                }
                return;
            }
            RecurringTransaction item = new RecurringTransaction(
                    userId,
                    amount,
                    type,
                    category.trim(),
                    note == null || note.trim().isEmpty() ? null : note.trim(),
                    frequency == null ? RecurrenceFrequency.MONTHLY : frequency,
                    firstRunAt,
                    true
            );
            recurringDao.insert(item);
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    public void delete(RecurringTransaction item) {
        executor.execute(() -> recurringDao.delete(item));
    }

    public void setActive(RecurringTransaction item, boolean active) {
        executor.execute(() -> {
            item.active = active;
            recurringDao.update(item);
        });
    }

    /** Creates due transactions for the logged-in user (safe to call often). */
    public void processDueForCurrentUser(Runnable onDone) {
        executor.execute(() -> {
            Long userId = sessionManager.getUserId();
            if (userId != null) {
                processDueInternal(userId, DateUtils.nowMillis());
            }
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    public void processDueInternal(long userId, long nowMillis) {
        List<RecurringTransaction> due = recurringDao.getDue(userId, nowMillis);
        for (RecurringTransaction item : due) {
            while (item.active && item.nextRunAt <= nowMillis) {
                Transaction tx = new Transaction(
                        userId,
                        item.amount,
                        item.type,
                        item.category,
                        item.note == null ? "Recurring" : item.note,
                        item.nextRunAt
                );
                tx.recurringId = item.id;
                transactionDao.insert(tx);
                item.nextRunAt = advance(item.nextRunAt, item.frequency);
            }
            recurringDao.update(item);
        }
    }

    public static long advance(long fromMillis, RecurrenceFrequency frequency) {
        LocalDate date = DateUtils.millisToLocalDate(fromMillis);
        if (frequency == RecurrenceFrequency.WEEKLY) {
            date = date.plusWeeks(1);
        } else {
            date = date.plusMonths(1);
        }
        return DateUtils.localDateToMillis(date);
    }
}

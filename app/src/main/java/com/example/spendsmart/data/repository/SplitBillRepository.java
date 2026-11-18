package com.example.spendsmart.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.spendsmart.data.local.dao.SplitBillDao;
import com.example.spendsmart.data.local.entity.SplitBill;
import com.example.spendsmart.data.session.SessionManager;
import com.example.spendsmart.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class SplitBillRepository {
    private final SplitBillDao dao;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public SplitBillRepository(SplitBillDao dao, SessionManager sessionManager,
                               ExecutorService executor) {
        this.dao = dao;
        this.sessionManager = sessionManager;
        this.executor = executor;
    }

    public LiveData<List<SplitBill>> observeAll() {
        return Transformations.switchMap(sessionManager.getCurrentUserIdLive(), userId -> {
            if (userId == null) {
                MutableLiveData<List<SplitBill>> empty = new MutableLiveData<>();
                empty.setValue(new ArrayList<>());
                return empty;
            }
            return dao.getAll(userId);
        });
    }

    public void add(String title, String friend, double total, double yourShare,
                    boolean friendOwesYou, Runnable onDone) {
        executor.execute(() -> {
            Long userId = sessionManager.getUserId();
            if (userId == null || title == null || title.trim().isEmpty()
                    || friend == null || friend.trim().isEmpty() || total <= 0) {
                if (onDone != null) {
                    onDone.run();
                }
                return;
            }
            dao.insert(new SplitBill(
                    userId,
                    title.trim(),
                    friend.trim(),
                    total,
                    yourShare <= 0 ? total / 2.0 : yourShare,
                    friendOwesYou,
                    false,
                    DateUtils.nowMillis()
            ));
            if (onDone != null) {
                onDone.run();
            }
        });
    }

    public void markSettled(SplitBill bill, boolean settled) {
        executor.execute(() -> {
            bill.settled = settled;
            dao.update(bill);
        });
    }

    public void delete(SplitBill bill) {
        executor.execute(() -> dao.delete(bill));
    }
}

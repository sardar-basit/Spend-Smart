package com.example.spendsmart.ui.split;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.spendsmart.data.local.entity.SplitBill;
import com.example.spendsmart.data.repository.SplitBillRepository;

import java.util.List;

public class SplitBillsViewModel extends ViewModel {
    private final SplitBillRepository repository;
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public SplitBillsViewModel(SplitBillRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<SplitBill>> getBills() {
        return repository.observeAll();
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void add(String title, String friend, String totalText, String shareText,
                    boolean friendOwesYou) {
        double total = parse(totalText);
        double share = parse(shareText);
        if (title == null || title.trim().isEmpty()
                || friend == null || friend.trim().isEmpty() || total <= 0) {
            message.setValue("Enter title, friend, and total");
            return;
        }
        repository.add(title, friend, total, share, friendOwesYou,
                () -> message.postValue("Split bill added"));
    }

    public void toggleSettled(SplitBill bill) {
        repository.markSettled(bill, !bill.settled);
    }

    public void delete(SplitBill bill) {
        repository.delete(bill);
    }

    private static double parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}

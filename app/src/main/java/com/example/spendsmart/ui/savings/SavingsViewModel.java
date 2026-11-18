package com.example.spendsmart.ui.savings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.spendsmart.data.local.entity.SavingsGoal;
import com.example.spendsmart.data.repository.SavingsGoalRepository;

import java.util.List;

public class SavingsViewModel extends ViewModel {
    private final SavingsGoalRepository repository;
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public SavingsViewModel(SavingsGoalRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<SavingsGoal>> getGoals() {
        return repository.observeAll();
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void addGoal(String title, String targetText, String savedText) {
        double target = parse(targetText);
        double saved = parse(savedText);
        if (title == null || title.trim().isEmpty() || target <= 0) {
            message.setValue("Enter a goal name and target amount");
            return;
        }
        repository.add(title, target, saved, null,
                () -> message.postValue("Goal added"));
    }

    public void addMoney(SavingsGoal goal, double amount) {
        if (amount <= 0) {
            return;
        }
        repository.addSavedAmount(goal, amount);
        message.setValue("Savings updated");
    }

    public void delete(SavingsGoal goal) {
        repository.delete(goal);
        message.setValue("Goal deleted");
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

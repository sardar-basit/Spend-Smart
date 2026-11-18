package com.example.spendsmart.ui.addtransaction;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.spendsmart.data.local.entity.PaymentMethod;
import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.data.repository.CategoryRepository;
import com.example.spendsmart.data.repository.TransactionRepository;
import com.example.spendsmart.domain.model.Categories;
import com.example.spendsmart.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class AddTransactionViewModel extends ViewModel {
    public static final int NO_ID = -1;

    private final TransactionRepository repository;
    private final ExecutorService executor;
    private final int transactionId;

    private final MutableLiveData<String> amountText = new MutableLiveData<>("");
    private final MutableLiveData<TransactionType> type =
            new MutableLiveData<>(TransactionType.EXPENSE);
    private final MutableLiveData<String> category =
            new MutableLiveData<>(Categories.EXPENSE.get(0));
    private final MutableLiveData<String> note = new MutableLiveData<>("");
    private final MutableLiveData<Long> dateMillis = new MutableLiveData<>(DateUtils.nowMillis());
    private final MutableLiveData<String> amountError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEditMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saveCompleted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> deleteCompleted = new MutableLiveData<>(false);
    private final MutableLiveData<String> receiptPath = new MutableLiveData<>(null);
    private final MutableLiveData<PaymentMethod> paymentMethod =
            new MutableLiveData<>(PaymentMethod.CASH);
    private final MediatorLiveData<List<String>> categories = new MediatorLiveData<>();

    private LiveData<List<String>> categorySource;
    private Integer existingRecurringId;

    public AddTransactionViewModel(TransactionRepository repository,
                                   CategoryRepository categoryRepository,
                                   ExecutorService executor,
                                   int transactionId) {
        this.repository = repository;
        this.executor = executor;
        this.transactionId = transactionId;
        observeCategories(categoryRepository, TransactionType.EXPENSE);
        type.observeForever(t -> {
            if (t != null) {
                observeCategories(categoryRepository, t);
            }
        });
        if (transactionId != NO_ID) {
            isEditMode.setValue(true);
            loadTransaction(transactionId);
        }
    }

    public LiveData<String> getAmountText() {
        return amountText;
    }

    public LiveData<TransactionType> getType() {
        return type;
    }

    public LiveData<String> getCategory() {
        return category;
    }

    public LiveData<List<String>> getCategories() {
        return categories;
    }

    public LiveData<String> getNote() {
        return note;
    }

    public LiveData<Long> getDateMillis() {
        return dateMillis;
    }

    public LiveData<String> getAmountError() {
        return amountError;
    }

    public LiveData<Boolean> getIsEditMode() {
        return isEditMode;
    }

    public LiveData<Boolean> getSaveCompleted() {
        return saveCompleted;
    }

    public LiveData<Boolean> getDeleteCompleted() {
        return deleteCompleted;
    }

    public LiveData<String> getReceiptPath() {
        return receiptPath;
    }

    public LiveData<PaymentMethod> getPaymentMethod() {
        return paymentMethod;
    }

    public void onAmountChange(String value) {
        String filtered = value == null ? "" : value.replaceAll("[^0-9.]", "");
        amountText.setValue(filtered);
        amountError.setValue(null);
    }

    public void onTypeChange(TransactionType t) {
        type.setValue(t);
    }

    public void onCategoryChange(String value) {
        category.setValue(value);
    }

    public void onNoteChange(String value) {
        note.setValue(value);
    }

    public void onDateChange(long millis) {
        dateMillis.setValue(millis);
    }

    public void setReceiptPath(String path) {
        receiptPath.setValue(path);
    }

    public void clearReceipt() {
        receiptPath.setValue(null);
    }

    public void onPaymentMethodChange(PaymentMethod method) {
        paymentMethod.setValue(method == null ? PaymentMethod.CASH : method);
    }

    public void save() {
        String text = amountText.getValue();
        Double amount = null;
        try {
            if (text != null && !text.isEmpty()) {
                amount = Double.parseDouble(text);
            }
        } catch (NumberFormatException ignored) {
        }
        if (amount == null || amount <= 0.0) {
            amountError.setValue("Enter an amount greater than 0");
            return;
        }
        TransactionType txType = type.getValue() != null ? type.getValue() : TransactionType.EXPENSE;
        String cat = category.getValue() != null ? category.getValue() : Categories.forType(txType).get(0);
        String noteVal = note.getValue();
        long date = dateMillis.getValue() != null ? dateMillis.getValue() : DateUtils.nowMillis();
        Transaction tx = new Transaction(0L, amount, txType, cat,
                noteVal == null || noteVal.trim().isEmpty() ? null : noteVal.trim(), date);
        tx.receiptUri = receiptPath.getValue();
        tx.recurringId = existingRecurringId;
        tx.paymentMethod = paymentMethod.getValue() != null
                ? paymentMethod.getValue() : PaymentMethod.CASH;
        if (Boolean.TRUE.equals(isEditMode.getValue())) {
            tx.id = transactionId;
            repository.update(tx, () -> saveCompleted.postValue(true));
        } else {
            repository.insert(tx, () -> saveCompleted.postValue(true));
        }
    }

    public void delete() {
        if (transactionId == NO_ID) {
            return;
        }
        executor.execute(() -> {
            Transaction existing = repository.getTransactionById(transactionId);
            if (existing != null) {
                repository.delete(existing, () -> deleteCompleted.postValue(true));
            }
        });
    }

    public void consumeSaveCompleted() {
        saveCompleted.setValue(false);
    }

    public void consumeDeleteCompleted() {
        deleteCompleted.setValue(false);
    }

    private void loadTransaction(int id) {
        executor.execute(() -> {
            Transaction existing = repository.getTransactionById(id);
            if (existing != null) {
                type.postValue(existing.type);
                amountText.postValue(String.valueOf(existing.amount));
                category.postValue(existing.category);
                note.postValue(existing.note == null ? "" : existing.note);
                dateMillis.postValue(existing.date);
                receiptPath.postValue(existing.receiptUri);
                existingRecurringId = existing.recurringId;
                paymentMethod.postValue(existing.paymentMethod != null
                        ? existing.paymentMethod : PaymentMethod.CASH);
                isEditMode.postValue(true);
            }
        });
    }

    private void observeCategories(CategoryRepository categoryRepository, TransactionType t) {
        if (categorySource != null) {
            categories.removeSource(categorySource);
        }
        categorySource = categoryRepository.observeCategories(t);
        categories.addSource(categorySource, list -> {
            List<String> cats = list != null ? list : new ArrayList<>(Categories.forType(t));
            categories.setValue(cats);
            String current = category.getValue();
            if (current == null || !cats.contains(current)) {
                if (!cats.isEmpty()) {
                    category.setValue(cats.get(0));
                }
            }
        });
    }
}

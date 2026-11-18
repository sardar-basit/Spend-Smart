package com.example.spendsmart.ui.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.data.repository.CategoryRepository;
import com.example.spendsmart.data.repository.TransactionRepository;
import com.example.spendsmart.util.DateUtils;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class HistoryViewModel extends ViewModel {
    private final TransactionRepository transactionRepository;
    private final MediatorLiveData<List<Transaction>> filtered = new MediatorLiveData<>();
    private final MutableLiveData<TypeFilter> typeFilter = new MutableLiveData<>(TypeFilter.ALL);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> categoryFilter = new MutableLiveData<>(null);
    private final MutableLiveData<YearMonth> monthFilter = new MutableLiveData<>(null);
    private final MutableLiveData<Double> minAmount = new MutableLiveData<>(null);
    private final MutableLiveData<Double> maxAmount = new MutableLiveData<>(null);
    private final MediatorLiveData<List<String>> categories = new MediatorLiveData<>();

    private List<Transaction> source = new ArrayList<>();
    private LiveData<List<Transaction>> currentSource;
    private Transaction recentlyDeleted;

    public HistoryViewModel(TransactionRepository transactionRepository,
                            CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        switchSource(null);

        LiveData<List<String>> expenseCats =
                categoryRepository.observeCategories(TransactionType.EXPENSE);
        LiveData<List<String>> incomeCats =
                categoryRepository.observeCategories(TransactionType.INCOME);
        categories.addSource(expenseCats, e -> mergeCategories(e, incomeCats.getValue()));
        categories.addSource(incomeCats, i -> mergeCategories(expenseCats.getValue(), i));

        filtered.addSource(typeFilter, f -> applyFilters());
        filtered.addSource(searchQuery, q -> applyFilters());
        filtered.addSource(categoryFilter, c -> applyFilters());
        filtered.addSource(minAmount, a -> applyFilters());
        filtered.addSource(maxAmount, a -> applyFilters());
        filtered.addSource(monthFilter, this::onMonthChanged);
    }

    public LiveData<List<Transaction>> getTransactions() {
        return filtered;
    }

    public LiveData<List<String>> getCategories() {
        return categories;
    }

    public LiveData<TypeFilter> getTypeFilter() {
        return typeFilter;
    }

    public LiveData<YearMonth> getMonthFilter() {
        return monthFilter;
    }

    public LiveData<String> getCategoryFilter() {
        return categoryFilter;
    }

    public void setTypeFilter(TypeFilter filter) {
        typeFilter.setValue(filter);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query == null ? "" : query);
    }

    public void setCategoryFilter(String category) {
        categoryFilter.setValue(category);
    }

    public void setMinAmount(Double value) {
        minAmount.setValue(value);
    }

    public void setMaxAmount(Double value) {
        maxAmount.setValue(value);
    }

    public void clearAmountFilters() {
        minAmount.setValue(null);
        maxAmount.setValue(null);
    }

    public void setMonthFilter(YearMonth yearMonth) {
        monthFilter.setValue(yearMonth);
    }

    public void deleteTransaction(Transaction transaction) {
        recentlyDeleted = transaction;
        transactionRepository.delete(transaction, null);
    }

    public void undoDelete() {
        if (recentlyDeleted == null) {
            return;
        }
        Transaction restore = recentlyDeleted;
        recentlyDeleted = null;
        Transaction copy = new Transaction(
                restore.userId, restore.amount, restore.type,
                restore.category, restore.note, restore.date
        );
        copy.receiptUri = restore.receiptUri;
        copy.recurringId = restore.recurringId;
        copy.paymentMethod = restore.paymentMethod;
        transactionRepository.insert(copy, null);
    }

    private void onMonthChanged(YearMonth yearMonth) {
        switchSource(yearMonth);
        applyFilters();
    }

    private void switchSource(YearMonth yearMonth) {
        if (currentSource != null) {
            filtered.removeSource(currentSource);
        }
        if (yearMonth != null) {
            currentSource = transactionRepository.getTransactionsByMonth(
                    DateUtils.startOfMonth(yearMonth), DateUtils.endOfMonth(yearMonth));
        } else {
            currentSource = transactionRepository.getAllTransactions();
        }
        filtered.addSource(currentSource, list -> {
            source = list != null ? list : new ArrayList<>();
            applyFilters();
        });
    }

    private void applyFilters() {
        TypeFilter type = typeFilter.getValue() != null ? typeFilter.getValue() : TypeFilter.ALL;
        String query = searchQuery.getValue() != null ? searchQuery.getValue().trim() : "";
        String category = categoryFilter.getValue();
        Double min = minAmount.getValue();
        Double max = maxAmount.getValue();
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : source) {
            boolean typeOk = type == TypeFilter.ALL
                    || (type == TypeFilter.INCOME && tx.type == TransactionType.INCOME)
                    || (type == TypeFilter.EXPENSE && tx.type == TransactionType.EXPENSE);
            boolean categoryOk = category == null || category.equals(tx.category);
            boolean searchOk = query.isEmpty()
                    || (tx.note != null && tx.note.toLowerCase(Locale.US)
                    .contains(query.toLowerCase(Locale.US)))
                    || tx.category.toLowerCase(Locale.US).contains(query.toLowerCase(Locale.US));
            boolean amountOk = (min == null || tx.amount >= min)
                    && (max == null || tx.amount <= max);
            if (typeOk && categoryOk && searchOk && amountOk) {
                result.add(tx);
            }
        }
        filtered.setValue(result);
    }

    private void mergeCategories(List<String> expense, List<String> income) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (expense != null) {
            set.addAll(expense);
        }
        if (income != null) {
            set.addAll(income);
        }
        categories.setValue(new ArrayList<>(set));
    }
}

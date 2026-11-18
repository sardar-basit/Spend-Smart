package com.example.spendsmart.domain.model;

import com.example.spendsmart.data.local.entity.TransactionType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Categories {
    public static final List<String> EXPENSE = Collections.unmodifiableList(Arrays.asList(
            "Food",
            "Transport",
            "Books",
            "Fees",
            "Rent",
            "Internet/Mobile Load",
            "Entertainment",
            "Other"
    ));

    public static final List<String> INCOME = Collections.unmodifiableList(Arrays.asList(
            "Pocket Money",
            "Allowance",
            "Scholarship",
            "Part-time",
            "Other"
    ));

    private Categories() {
    }

    public static List<String> forType(TransactionType type) {
        return type == TransactionType.INCOME ? INCOME : EXPENSE;
    }
}

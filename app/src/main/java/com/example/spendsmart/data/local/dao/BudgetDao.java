package com.example.spendsmart.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.spendsmart.data.local.entity.Budget;

@Dao
public interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(Budget budget);

    @Query("SELECT * FROM budgets WHERE userId = :userId LIMIT 1")
    LiveData<Budget> observeBudget(long userId);

    @Query("SELECT * FROM budgets WHERE userId = :userId LIMIT 1")
    Budget getBudget(long userId);
}

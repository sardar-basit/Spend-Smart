package com.example.spendsmart.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.spendsmart.data.local.entity.CategoryBudget;

import java.util.List;

@Dao
public interface CategoryBudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CategoryBudget budget);

    @Delete
    void delete(CategoryBudget budget);

    @Query("SELECT * FROM category_budgets WHERE userId = :userId ORDER BY category ASC")
    LiveData<List<CategoryBudget>> getAll(long userId);

    @Query("SELECT * FROM category_budgets WHERE userId = :userId ORDER BY category ASC")
    List<CategoryBudget> getAllSync(long userId);

    @Query("SELECT * FROM category_budgets WHERE userId = :userId AND category = :category LIMIT 1")
    CategoryBudget getByCategory(long userId, String category);

    @Query("DELETE FROM category_budgets WHERE userId = :userId AND category = :category")
    void deleteByCategory(long userId, String category);
}

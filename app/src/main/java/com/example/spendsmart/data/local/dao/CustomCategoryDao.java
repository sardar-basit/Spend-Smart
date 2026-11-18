package com.example.spendsmart.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.spendsmart.data.local.entity.CustomCategory;
import com.example.spendsmart.data.local.entity.TransactionType;

import java.util.List;

@Dao
public interface CustomCategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(CustomCategory category);

    @Delete
    void delete(CustomCategory category);

    @Query("SELECT * FROM custom_categories WHERE userId = :userId ORDER BY name ASC")
    LiveData<List<CustomCategory>> observeAll(long userId);

    @Query("SELECT * FROM custom_categories WHERE userId = :userId AND type = :type "
            + "ORDER BY name ASC")
    LiveData<List<CustomCategory>> observeByType(long userId, TransactionType type);

    @Query("SELECT * FROM custom_categories WHERE userId = :userId AND type = :type "
            + "ORDER BY name ASC")
    List<CustomCategory> getByTypeSync(long userId, TransactionType type);
}

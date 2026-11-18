package com.example.spendsmart.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.spendsmart.data.local.entity.SavingsGoal;

import java.util.List;

@Dao
public interface SavingsGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SavingsGoal goal);

    @Update
    void update(SavingsGoal goal);

    @Delete
    void delete(SavingsGoal goal);

    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<SavingsGoal>> getAll(long userId);

    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY id DESC")
    List<SavingsGoal> getAllSync(long userId);
}

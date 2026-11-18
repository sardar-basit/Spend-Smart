package com.example.spendsmart.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.spendsmart.data.local.entity.RecurringTransaction;

import java.util.List;

@Dao
public interface RecurringTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(RecurringTransaction item);

    @Update
    void update(RecurringTransaction item);

    @Delete
    void delete(RecurringTransaction item);

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId ORDER BY nextRunAt ASC")
    LiveData<List<RecurringTransaction>> getAll(long userId);

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId AND active = 1 "
            + "AND nextRunAt <= :beforeMillis ORDER BY nextRunAt ASC")
    List<RecurringTransaction> getDue(long userId, long beforeMillis);

    @Query("SELECT * FROM recurring_transactions WHERE active = 1 AND nextRunAt <= :beforeMillis")
    List<RecurringTransaction> getAllDue(long beforeMillis);
}

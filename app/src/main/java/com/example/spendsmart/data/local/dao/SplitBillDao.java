package com.example.spendsmart.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.spendsmart.data.local.entity.SplitBill;

import java.util.List;

@Dao
public interface SplitBillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SplitBill bill);

    @Update
    void update(SplitBill bill);

    @Delete
    void delete(SplitBill bill);

    @Query("SELECT * FROM split_bills WHERE userId = :userId ORDER BY dateMillis DESC")
    LiveData<List<SplitBill>> getAll(long userId);
}

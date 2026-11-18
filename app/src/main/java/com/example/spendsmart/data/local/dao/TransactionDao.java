package com.example.spendsmart.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.spendsmart.data.local.entity.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions(long userId);

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    List<Transaction> getAllTransactionsSync(long userId);

    @Query("SELECT * FROM transactions WHERE userId = :userId "
            + "AND date >= :startOfMonth AND date < :endOfMonth ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByMonth(long userId, long startOfMonth, long endOfMonth);

    @Query("SELECT * FROM transactions WHERE userId = :userId "
            + "AND date >= :startOfMonth AND date < :endOfMonth ORDER BY date DESC")
    List<Transaction> getTransactionsByMonthSync(long userId, long startOfMonth, long endOfMonth);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND category = :category "
            + "ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(long userId, String category);

    @Query("SELECT * FROM transactions WHERE id = :id AND userId = :userId LIMIT 1")
    Transaction getTransactionById(int id, long userId);
}

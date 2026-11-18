package com.example.spendsmart.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.spendsmart.data.local.entity.User;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getById(long id);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int countByEmail(String email);
}

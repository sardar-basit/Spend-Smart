package com.example.spendsmart.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {@Index(value = {"email"}, unique = true)}
)
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String email;
    public String passwordHash;
    public String salt;
    public long createdAt;

    public User(String name, String email, String passwordHash, String salt) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.createdAt = System.currentTimeMillis();
    }
}

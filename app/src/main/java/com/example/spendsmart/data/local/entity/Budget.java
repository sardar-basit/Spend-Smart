package com.example.spendsmart.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "budgets",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = {"userId"}, unique = true)}
)
public class Budget {
    @PrimaryKey
    public long userId;
    public double monthlyLimit;
    public double savingsGoal;

    public Budget(long userId, double monthlyLimit, double savingsGoal) {
        this.userId = userId;
        this.monthlyLimit = monthlyLimit;
        this.savingsGoal = savingsGoal;
    }
}

package com.example.spendsmart.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "savings_goals",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class SavingsGoal {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long userId;
    @NonNull
    public String title;
    public double targetAmount;
    public double savedAmount;
    public Long deadlineMillis;

    public SavingsGoal(long userId, @NonNull String title, double targetAmount,
                       double savedAmount, Long deadlineMillis) {
        this.userId = userId;
        this.title = title;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.deadlineMillis = deadlineMillis;
    }
}

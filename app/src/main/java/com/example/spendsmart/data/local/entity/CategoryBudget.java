package com.example.spendsmart.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "category_budgets",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("userId"),
                @Index(value = {"userId", "category"}, unique = true)
        }
)
public class CategoryBudget {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long userId;
    @NonNull
    public String category = "";
    public double monthlyLimit;

    public CategoryBudget(long userId, @NonNull String category, double monthlyLimit) {
        this.userId = userId;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
    }
}

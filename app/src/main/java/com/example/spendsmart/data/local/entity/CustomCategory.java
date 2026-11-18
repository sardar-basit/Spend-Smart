package com.example.spendsmart.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "custom_categories",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("userId"),
                @Index(value = {"userId", "name", "type"}, unique = true)
        }
)
public class CustomCategory {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String name;
    public TransactionType type;

    public CustomCategory(long userId, String name, TransactionType type) {
        this.userId = userId;
        this.name = name;
        this.type = type;
    }
}

package com.example.spendsmart.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "recurring_transactions",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId"), @Index("nextRunAt")}
)
public class RecurringTransaction {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long userId;
    public double amount;
    public TransactionType type;
    public String category;
    public String note;
    public RecurrenceFrequency frequency;
    public long nextRunAt;
    public boolean active;

    public RecurringTransaction(long userId, double amount, TransactionType type,
                                String category, String note,
                                RecurrenceFrequency frequency, long nextRunAt, boolean active) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.note = note;
        this.frequency = frequency;
        this.nextRunAt = nextRunAt;
        this.active = active;
    }
}

package com.example.spendsmart.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "transactions",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId"), @Index("date")}
)
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long userId;
    public double amount;
    public TransactionType type;
    public String category;
    public String note;
    public long date;
    public String receiptUri;
    public Integer recurringId;
    public PaymentMethod paymentMethod;

    public Transaction(long userId, double amount, TransactionType type,
                       String category, String note, long date) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.note = note;
        this.date = date;
        this.receiptUri = null;
        this.recurringId = null;
        this.paymentMethod = PaymentMethod.CASH;
    }
}

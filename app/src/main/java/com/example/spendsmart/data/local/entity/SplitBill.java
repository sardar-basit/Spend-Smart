package com.example.spendsmart.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "split_bills",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class SplitBill {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long userId;
    @NonNull
    public String title;
    @NonNull
    public String friendName;
    public double totalAmount;
    public double yourShare;
    /** true = friend owes you; false = you owe friend */
    public boolean friendOwesYou;
    public boolean settled;
    public long dateMillis;

    public SplitBill(long userId, @NonNull String title, @NonNull String friendName,
                     double totalAmount, double yourShare, boolean friendOwesYou,
                     boolean settled, long dateMillis) {
        this.userId = userId;
        this.title = title;
        this.friendName = friendName;
        this.totalAmount = totalAmount;
        this.yourShare = yourShare;
        this.friendOwesYou = friendOwesYou;
        this.settled = settled;
        this.dateMillis = dateMillis;
    }
}

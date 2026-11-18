package com.example.spendsmart.data.local;

import androidx.room.TypeConverter;

import com.example.spendsmart.data.local.entity.PaymentMethod;
import com.example.spendsmart.data.local.entity.RecurrenceFrequency;
import com.example.spendsmart.data.local.entity.TransactionType;

public class Converters {
    @TypeConverter
    public String fromTransactionType(TransactionType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public TransactionType toTransactionType(String value) {
        return value == null ? null : TransactionType.valueOf(value);
    }

    @TypeConverter
    public String fromRecurrenceFrequency(RecurrenceFrequency frequency) {
        return frequency == null ? null : frequency.name();
    }

    @TypeConverter
    public RecurrenceFrequency toRecurrenceFrequency(String value) {
        return value == null ? null : RecurrenceFrequency.valueOf(value);
    }

    @TypeConverter
    public String fromPaymentMethod(PaymentMethod method) {
        return method == null ? null : method.name();
    }

    @TypeConverter
    public PaymentMethod toPaymentMethod(String value) {
        if (value == null) {
            return PaymentMethod.CASH;
        }
        try {
            return PaymentMethod.valueOf(value);
        } catch (Exception e) {
            return PaymentMethod.CASH;
        }
    }
}

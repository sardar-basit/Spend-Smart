package com.example.spendsmart.util;

import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.TransactionType;

import java.util.List;

public final class CsvExporter {
    private CsvExporter() {
    }

    public static String toCsv(List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder("Date,Type,Category,Amount,Payment,Note\n");
        for (Transaction tx : transactions) {
            String date = DateUtils.formatDisplayDate(tx.date);
            String type = tx.type.name();
            String category = escape(tx.category);
            double amount = tx.type == TransactionType.EXPENSE ? -tx.amount : tx.amount;
            String payment = tx.paymentMethod == null ? "CASH" : tx.paymentMethod.name();
            String note = escape(tx.note == null ? "" : tx.note);
            sb.append(date).append(',')
                    .append(type).append(',')
                    .append(category).append(',')
                    .append(amount).append(',')
                    .append(payment).append(',')
                    .append(note).append('\n');
        }
        return sb.toString();
    }

    private static String escape(String value) {
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }
}

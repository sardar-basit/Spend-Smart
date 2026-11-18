package com.example.spendsmart.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import com.example.spendsmart.data.local.entity.Budget;
import com.example.spendsmart.data.local.entity.CategoryBudget;
import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.TransactionType;

import java.io.File;
import java.io.FileOutputStream;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PdfExporter {
    private PdfExporter() {
    }

    public static File exportMonthlyReport(File dest,
                                           String userName,
                                           YearMonth month,
                                           List<Transaction> transactions,
                                           Budget budget,
                                           List<CategoryBudget> categoryBudgets) throws Exception {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint title = new Paint();
        title.setTextSize(20f);
        title.setFakeBoldText(true);
        Paint body = new Paint();
        body.setTextSize(11f);
        Paint muted = new Paint();
        muted.setTextSize(10f);
        muted.setColor(0xFF666666);

        float x = 40f;
        float y = 50f;
        canvas.drawText("SpendSmart Monthly Report", x, y, title);
        y += 28f;
        canvas.drawText("User: " + (userName == null ? "-" : userName), x, y, body);
        y += 18f;
        canvas.drawText("Period: " + DateUtils.formatMonthYear(month), x, y, body);
        y += 24f;

        double income = 0;
        double expense = 0;
        Map<String, Double> byCategory = new HashMap<>();
        for (Transaction tx : transactions) {
            if (tx.type == TransactionType.INCOME) {
                income += tx.amount;
            } else {
                expense += tx.amount;
                Double prev = byCategory.get(tx.category);
                byCategory.put(tx.category, (prev == null ? 0.0 : prev) + tx.amount);
            }
        }
        double balance = income - expense;

        canvas.drawText("Income: " + CurrencyFormatter.format(income), x, y, body);
        y += 16f;
        canvas.drawText("Expense: " + CurrencyFormatter.format(expense), x, y, body);
        y += 16f;
        canvas.drawText("Net: " + CurrencyFormatter.format(balance), x, y, body);
        y += 16f;
        if (budget != null && budget.monthlyLimit > 0) {
            canvas.drawText("Monthly budget: " + CurrencyFormatter.format(budget.monthlyLimit)
                    + "  (" + ((int) Math.min(100, (expense / budget.monthlyLimit) * 100))
                    + "% used)", x, y, body);
            y += 16f;
        }
        if (budget != null && budget.savingsGoal > 0) {
            double saved = Math.max(0, income - expense);
            canvas.drawText("Savings goal: " + CurrencyFormatter.format(saved)
                    + " / " + CurrencyFormatter.format(budget.savingsGoal), x, y, body);
            y += 16f;
        }

        y += 10f;
        canvas.drawText("Category breakdown", x, y, title);
        y += 20f;
        if (byCategory.isEmpty()) {
            canvas.drawText("No expenses this month.", x, y, muted);
            y += 16f;
        } else {
            for (Map.Entry<String, Double> entry : byCategory.entrySet()) {
                String line = String.format(Locale.US, "%s: %s",
                        entry.getKey(), CurrencyFormatter.format(entry.getValue()));
                if (categoryBudgets != null) {
                    for (CategoryBudget cb : categoryBudgets) {
                        if (cb.category.equals(entry.getKey())) {
                            line += " / budget " + CurrencyFormatter.format(cb.monthlyLimit);
                            break;
                        }
                    }
                }
                canvas.drawText(line, x, y, body);
                y += 15f;
                if (y > 780) {
                    break;
                }
            }
        }

        y += 12f;
        canvas.drawText("Transactions", x, y, title);
        y += 18f;
        int shown = 0;
        for (Transaction tx : transactions) {
            if (shown >= 28 || y > 800) {
                canvas.drawText("…and more", x, y, muted);
                break;
            }
            String sign = tx.type == TransactionType.INCOME ? "+" : "-";
            String line = DateUtils.formatDisplayDate(tx.date)
                    + "  " + tx.category
                    + "  " + sign + CurrencyFormatter.format(tx.amount);
            canvas.drawText(line, x, y, body);
            y += 14f;
            shown++;
        }

        document.finishPage(page);
        try (FileOutputStream out = new FileOutputStream(dest)) {
            document.writeTo(out);
        } finally {
            document.close();
        }
        return dest;
    }
}

package com.example.spendsmart.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.spendsmart.R;

public class BudgetNotifier {
    public static final String CHANNEL_ID = "budget_alerts";
    private static final int NOTIFICATION_ID_WARNING = 1001;
    private static final int NOTIFICATION_ID_OVER = 1002;
    private static final int NOTIFICATION_ID_CATEGORY_BASE = 1100;

    private final Context context;

    public BudgetNotifier(Context context) {
        this.context = context.getApplicationContext();
    }

    public void ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Budget alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Alerts when you approach or exceed your monthly budget");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void maybeNotify(double spent, double limit) {
        if (limit <= 0.0) {
            return;
        }
        double ratio = spent / limit;
        if (ratio >= 1.0) {
            show(NOTIFICATION_ID_OVER, "Over budget",
                    "You've spent " + CurrencyFormatter.format(spent)
                            + " of " + CurrencyFormatter.format(limit) + " this month.");
        } else if (ratio >= 0.8) {
            show(NOTIFICATION_ID_WARNING, "Budget warning",
                    "You've used " + ((int) (ratio * 100)) + "% of your monthly budget.");
        }
    }

    public void maybeNotifyCategory(String category, double spent, double limit) {
        if (limit <= 0.0 || category == null) {
            return;
        }
        double ratio = spent / limit;
        int id = NOTIFICATION_ID_CATEGORY_BASE + Math.abs(category.hashCode() % 80);
        if (ratio >= 1.0) {
            show(id, category + " over budget",
                    "You've spent " + CurrencyFormatter.format(spent)
                            + " of " + CurrencyFormatter.format(limit)
                            + " on " + category + ".");
        } else if (ratio >= 0.8) {
            show(id, category + " budget warning",
                    "You've used " + ((int) (ratio * 100)) + "% of your "
                            + category + " budget.");
        }
    }

    private void show(int id, String title, String body) {
        if (!canPostNotifications()) {
            return;
        }
        ensureChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat.from(context).notify(id, builder.build());
    }

    private boolean canPostNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }
}

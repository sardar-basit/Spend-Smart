package com.example.spendsmart.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.spendsmart.MainActivity;
import com.example.spendsmart.R;

public class QuickAddWidgetProvider extends AppWidgetProvider {
    public static final String EXTRA_OPEN_ADD = "open_add_transaction";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_quick_add);
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(EXTRA_OPEN_ADD, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pending = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widgetRoot, pending);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}

package com.ihs.inputmethod.uimodules.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by ihandysoft on 16/8/16.
 */
public class HSAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        remoteViews.setImageViewResource(R.id.app_icon, context.getResources().getIdentifier("ic_launcher", "drawable", context.getPackageName()));
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.app_icon, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}

package com.ihs.booster.utils;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

public class GAnalyticsUtils {

    // Google analytics
    public static GoogleAnalytics mAnalytics;
    public static Tracker mTracker;
    public static String googleKey;

    private GAnalyticsUtils() {
    }

    public static void init(final Context context) {
        mAnalytics = GoogleAnalytics.getInstance(context);
        mAnalytics.setLocalDispatchPeriod(120);
        initTracker();
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                initTracker();
            }
        });
    }

    private static String getTrackingId() {
        return HSConfig.getString("libCommons", "Analytics", "GoogleTrackerID");
    }

    private static void initTracker() {
        String new_googleKey = getTrackingId();
        if (!TextUtils.equals(googleKey, new_googleKey) || mTracker == null) {
            googleKey = new_googleKey;
            mTracker = mAnalytics.newTracker(getTrackingId());
            mTracker.enableExceptionReporting(true);
            mTracker.enableAutoActivityTracking(true);
            mTracker.setScreenName("Memory_Boost");
        }
    }

    public static void logEvent(String category, String action) {
        if (mTracker != null && !HSLog.isDebugging()) {
            mTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).build());
            HSLog.d("Google LogEvent -- category:" + category + " action:" + action);
        }
    }

    public static void logEvent(String category, String action, String label) {
        if (mTracker != null && !HSLog.isDebugging()) {
            mTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
            HSLog.d("Google LogEvent -- category:" + category + " action:" + action + " label:" + label);
        }
    }
}

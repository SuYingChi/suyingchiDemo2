package com.keyboard.common;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.ihs.app.framework.HSApplication;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ActivityLifecycleMonitor {
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static int activityCount = 0;
    private static ActivityLifecycleCallbacks callbacks = new ActivityLifecycleCallbacks() {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            activityCount ++;

            if (activityCount == 1) {
                onEnterForeground();
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            activityCount --;

            if (activityCount == 0) {
                onEnterBackground();
            }
        }
    };

    public static void startMonitor(Application application) {
        application.registerActivityLifecycleCallbacks(callbacks);
    }

    private static void onEnterForeground() {
        handler.removeCallbacks(clearImageLoaderCacheRunnable);
    }

    private static void onEnterBackground() {
        long delayMillis = 5 * 60 * 1000;
        if (HSApplication.isDebugging) {
            delayMillis = 5 * 1000;
        }

        handler.postDelayed(clearImageLoaderCacheRunnable, delayMillis);
    }

    private static Runnable clearImageLoaderCacheRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                ImageLoader.getInstance().clearMemoryCache();
                Glide.get(HSApplication.getContext()).clearMemory();
                System.gc();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}

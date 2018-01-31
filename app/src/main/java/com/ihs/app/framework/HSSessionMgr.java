package com.ihs.app.framework;

import android.app.Activity;

/**
 * Created by Arthur on 18/1/17.
 */

public class HSSessionMgr {
    private static boolean sessionStarted;
    private static int currentSessionId;
    private static long firstSessionStartTime;

    public static boolean isSessionStarted() {
        return sessionStarted;
    }

    public static void onActivityCreate(Activity hsAppCompatActivity) {

    }

    public static void onActivityDestroy(Activity hsAppCompatActivity) {

    }

    public static void onActivityStart(Activity hsAppCompatActivity) {

    }

    public static void onActivityStop(Activity hsAppCompatActivity, boolean isBackPressed) {


    }

    public static int getCurrentSessionId() {
        return 1;
    }

    public static long getFirstSessionStartTime() {
        return 0;
    }

}

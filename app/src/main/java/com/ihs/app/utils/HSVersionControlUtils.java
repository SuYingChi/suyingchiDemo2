package com.ihs.app.utils;

/**
 * Created by Arthur on 18/1/17.
 */

public class HSVersionControlUtils {
    private static boolean firstLaunchSinceUpgrade;
    private static boolean firstLaunchSinceInstallation;
    private static boolean firstSessionSinceInstallation;

    public static boolean isFirstLaunchSinceUpgrade() {
        return false;
    }

    public static int getAppVersionCode() {
        return 0;
    }

    public static boolean isFirstLaunchSinceInstallation() {
        return true;
    }

    public static boolean isFirstSessionSinceInstallation() {
        return firstSessionSinceInstallation;
    }
}

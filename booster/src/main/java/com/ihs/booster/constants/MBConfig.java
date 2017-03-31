package com.ihs.booster.constants;

import com.ihs.commons.utils.HSLog;

/**
 * Created by sharp on 15/8/12.
 */
public class MBConfig {

    public static final boolean ENABLE_BOOST=true;
    public static final boolean ENABLE_CHARGING=true;
    public static final int CPU_FAKE_TEMP_EXPIRED_SECOND = 60;
    public static final long CLEAN_TOAST_DURATION = 2 * 1000;
    public static final long PERMISSION_TIP_HAND_DURATION = 1000 + 500;
    public static final long PERMISSION_TIP_DISAPPEAR_TIME = 4 * 1000;
    public static final String FLAG_START_MAIN_FRAGMENT  = "startMainFrag";

    public static final int CLEAN_EXPIRED_SECOND = HSLog.isDebugging() ? 30 : 120;
    public static final int CLEAN_LONG_EXPIRED_SECOND = HSLog.isDebugging() ? 30 : 240;
    public static final int SCAN_EXPIRED_SECOND = HSLog.isDebugging() ? 20 : 60;
    public static final int MEMORY_PROTECT_ALERT_DELAY_MILLISECOND = 1500;
    public static final int RATE_ALERT_DELAY_MILLISECOND = 100;
    public static int CLEAN_JUNK_EXPIRED_SECOND = CLEAN_EXPIRED_SECOND;
    public static int CLEAN_MEMORY_EXPIRED_SECOND = CLEAN_EXPIRED_SECOND;
    public static int CLEAN_BATTERY_EXPIRED_SECOND = CLEAN_EXPIRED_SECOND;

    public static void resetExpiredTime() {
        CLEAN_JUNK_EXPIRED_SECOND = CLEAN_EXPIRED_SECOND;
        CLEAN_MEMORY_EXPIRED_SECOND = CLEAN_EXPIRED_SECOND;
        CLEAN_BATTERY_EXPIRED_SECOND = CLEAN_EXPIRED_SECOND;
    }

    public static void changeJunkExpiredTime() {
        CLEAN_JUNK_EXPIRED_SECOND = CLEAN_LONG_EXPIRED_SECOND;
    }

    public static void changeMemoryExpiredTime() {
        CLEAN_MEMORY_EXPIRED_SECOND = CLEAN_LONG_EXPIRED_SECOND;
    }

    public static void changeBatteryExpiredTime() {
        CLEAN_BATTERY_EXPIRED_SECOND = CLEAN_LONG_EXPIRED_SECOND;
    }
}

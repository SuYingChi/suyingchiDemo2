package com.ihs.booster.boost.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.booster.boost.memory.MemoryPrefsManager;
import com.ihs.booster.constants.MBConfig;
import com.ihs.booster.utils.L;
import com.ihs.booster.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class PrefsUtils {
    // cpu
    private static final String PREFS_CPU_COOL_DOWN_TEMP = "CPU_COOL_DOWN_TEMP";
    private static final String PREFS_CPU_COOLED_APP_NUMBER = "CPU_COOLED_APP_NUMBER";
    private static final String PREFS_CPU_DECREASE_TEMP = "CPU_DECREASE_TEMP";
    private static final String PREFS_CPU_LAST_COOLED_TIME = "LAST_CPU_COOLED_TIME";

    private static final String PREFS_LAST_MEMORY_BOOSTED_TIME = "LAST_MEMORY_BOOSTED_TIME";
    private static final String PREFS_LAST_BATTERY_BOOSTED_TIME = "LAST_BATTERY_BOOSTED_TIME";
    private static final String PREFS_LAST_JUNK_CLEANED_TIME = "LAST_JUNK_CLEANED_TIME";
    private static final String PREFS_JUNK_CLEANED_TOTAL_SIZE = "JUNK_CLEANED_TOTAL_SIZE";
    private static final String PREFS_JUNK_CLEANED_CURRENT_SIZE = "JUNK_CLEANED_CURRENT_SIZE";
    private static final String PREFS_MEMORY_BOOSTED_PERCENT = "MEMORY_BOOSTED_PERCENT";
    private static final String PREFS_MEMORY_BOOSTED_SIZE = "MEMORY_BOOSTED_SIZE";
    private static final String PREFS_BATTERY_SAVED_MINS = "BATTERY_SAVED_TIME";
    private static final String PREFS_HAS_SET_SHORTCUT = "HAS_SET_SHORTCUT";
    private static final String PREFS_MEMORY_PROTECTION = "MEMORY_PROTECTION";
    private static final String PREFS_NOTIFY_TOOLBAR = "NOTIFY_TOOLBAR";
    private static final String PREFS_TEMPERATURE_IN_F = "TEMPERATURE_IN_F";

    private static final String PREFS_ALARM_TIMES_TODAY = "ALARM_TIMES_TODAY";
    private static final String PREFS_LAST_ALARM_TIME = "LAST_ALARM_TIME";
    private static final String PREFS_LAST_SESSION_END_TIME = "LAST_SESSION_END_TIME";
    private static final String PREFS_VERSION_RATE_FLAG = "VERSION_RATE_FLAG";
    private static final String PREFS_SESSIONID_OFFSET = "SESSIONID_OFFSET";
    private static final String PREFS_RATE_ALERT_SHOWED_FLAG = "RATE_ALERT_SHOWED_FLAG";
    private static final String PREFS_RATE_ALERT_SHOWED_COUNT = "RATE_ALERT_SHOWED_COUNT";
    private static final String PREFS_RATE_ALERT_SHOWED_SESSION_ID = "RATE_ALERT_SHOWED_SESSION_ID";
    private static final String PREFS_MEMORY_PROTECT_ALERT_SHOWED_COUNT = "PREFS_MEMORY_PROTECT_ALERT_SHOWED_COUNT";
    private static final String PREFS_BATTERY_SAVE_MODE_ALERT_SHOW_TOTAL_COUNT = "BATERY_SAVE_MODE_ALERT_SHOW_COUNT";
    //battery alert showed count, for show next battery alert
    private static final String PREFS_BATTERY_SAVE_MODE_ALERT_SHOW_COUNT = "PREFS_BATTERY_SAVE_MODE_ALERT_SHOW_COUNT";
    private static final String PREFS_LAST_CPU_TIME_TOTAL = "LAST_CPU_TIME_TOTAL";

    private static final String PRES_MAIN_FRAGMENT_DASHBOARBAR_CLICK_COUNT = "MAIN_FRAGMENT_DASHBOARBAR_CLICK_COUNT";
    private static final String PRES_MAIN_FRAGMENT_BOOST_IMAGEVIEW_CLICK_COUNT = "PRES_MAIN_FRAGMENT_BOOST_IMAGEVIEW_CLICK_COUNT";
    private static final String PRES_MAIN_FRAGMENT_MEMORY_CLICK_COUNT = "PRES_MAIN_FRAGMENT_MEMORY_CLICK_COUNT";
    private static final String PRES_MAIN_FRAGMENT_JUNK_CLICK_COUNT = "PRES_MAIN_FRAGMENT_JUNK_CLICK_COUNT";
    private static final String PRES_MAIN_FRAGMENT_BATTERY_CLICK_COUNT = "PRES_MAIN_FRAGMENT_BATTERY_CLICK_COUNT";
    private static final String PRES_BATTERY_FRAGMENT_CLEAN_BUTTON_CLICK_TOTAL_COUNT = "PRES_BATTERY_FRAGMENT_CLEAN_BUTTON_CLICK_TOTAL_COUNT";

    //battery fragment optimize button click count, for show battery alert.
    private static final String PRES_BATTERY_OPTIMIZE_BUTTON_CLICK_COUNT = "PRES_BATTERY_OPTIMIZE_BUTTON_CLICK_COUNT";
    private static final String PRES_MEMORY_FRAGMENT_BOOST_BUTTON_CLICK_COUNT = "PRES_MEMORY_FRAGMENT_BOOST_BUTTON_CLICK_COUNT";
    private static final String PRES_JUNK_FRAGMENT_CLEAN_BUTTON_CLICK_COUNT = "PRES_JUNK_FRAGMENT_CLEAN_BUTTON_CLICK_COUNT";
    private static final String PRES_RECENT_AD_START_DISPLAY_TIME = "PRES_RECENT_AD_START_DISPLAY_TIME";

    public static void setPresRecentAdStartDisplayTime() {
        long curTime = System.currentTimeMillis();
        getSharedPreferences().edit().putLong(PRES_RECENT_AD_START_DISPLAY_TIME, curTime).commit();
    }

    public static long getPresRecentAdStartDisplayTime() {
        return getSharedPreferences().getLong(PRES_RECENT_AD_START_DISPLAY_TIME, 0);
    }

    public static int getMainFragmentDashboarbarClickCount() {
        return getSharedPreferences().getInt(PRES_MAIN_FRAGMENT_DASHBOARBAR_CLICK_COUNT, 0);
    }

    public static void addMainFragmentDashboarbarClickCount() {
        int count = getMainFragmentDashboarbarClickCount() + 1;
        getSharedPreferences().edit().putInt(PRES_MAIN_FRAGMENT_DASHBOARBAR_CLICK_COUNT, count).commit();
    }

    public static int getMainFragmentBoostImageViewClickCount() {
        return getSharedPreferences().getInt(PRES_MAIN_FRAGMENT_BOOST_IMAGEVIEW_CLICK_COUNT, 0);
    }

    public static void addMainFragmentBoostImageViewClickCount() {
        int count = getMainFragmentBoostImageViewClickCount() + 1;
        getSharedPreferences().edit().putInt(PRES_MAIN_FRAGMENT_BOOST_IMAGEVIEW_CLICK_COUNT, count).commit();
    }

    public static int getMainFragmentMemoryClickCount() {
        return getSharedPreferences().getInt(PRES_MAIN_FRAGMENT_MEMORY_CLICK_COUNT, 0);
    }

    public static void addMainFragmentMemoryClickCount() {
        int count = getMainFragmentMemoryClickCount() + 1;
        getSharedPreferences().edit().putInt(PRES_MAIN_FRAGMENT_MEMORY_CLICK_COUNT, count).commit();
    }

    public static int getMainFragmentJunkButtonClickCount() {
        return getSharedPreferences().getInt(PRES_MAIN_FRAGMENT_JUNK_CLICK_COUNT, 0);
    }

    public static void addMainFragmentJunkButtonClickCount() {
        int count = getMainFragmentJunkButtonClickCount() + 1;
        getSharedPreferences().edit().putInt(PRES_MAIN_FRAGMENT_JUNK_CLICK_COUNT, count).commit();
    }

    public static int getMainFragmentBatteryButtonClickCount() {
        return getSharedPreferences().getInt(PRES_MAIN_FRAGMENT_BATTERY_CLICK_COUNT, 0);
    }

    public static void addMainFragmentBatteryButtonClickCount() {
        int count = getMainFragmentBatteryButtonClickCount() + 1;
        getSharedPreferences().edit().putInt(PRES_MAIN_FRAGMENT_BATTERY_CLICK_COUNT, count).commit();
    }

    public static int getBatteryOptimizeButtonClickCount() {
        return getSharedPreferences().getInt(PRES_BATTERY_OPTIMIZE_BUTTON_CLICK_COUNT, 0);
    }

    public static void addBatteryOptimizeButtonClickCount() {
        int count = getBatteryOptimizeButtonClickCount() + 1;
        int totalCount = getBatteryFragmentCleanButtonClickTotalCount() + 1;
        Editor editor = getSharedPreferences().edit();
        editor.putInt(PRES_BATTERY_OPTIMIZE_BUTTON_CLICK_COUNT, count);
        editor.putInt(PRES_BATTERY_FRAGMENT_CLEAN_BUTTON_CLICK_TOTAL_COUNT, totalCount).commit();
    }

    public static int getBatteryFragmentCleanButtonClickTotalCount() {
        return getSharedPreferences().getInt(PRES_BATTERY_FRAGMENT_CLEAN_BUTTON_CLICK_TOTAL_COUNT, 0);
    }

    public static int getMemoryFragmentBoostButtonClickCount() {
        return getSharedPreferences().getInt(PRES_MEMORY_FRAGMENT_BOOST_BUTTON_CLICK_COUNT, 0);
    }

    public static void addMemoryFragmentBoostButtonClickCount() {
        int count = getMemoryFragmentBoostButtonClickCount() + 1;
        getSharedPreferences().edit().putInt(PRES_MEMORY_FRAGMENT_BOOST_BUTTON_CLICK_COUNT, count).commit();
    }

    public static int getJunkFragmentCleanButtonClickCount() {
        return getSharedPreferences().getInt(PRES_JUNK_FRAGMENT_CLEAN_BUTTON_CLICK_COUNT, 0);
    }

    public static void addJunkFragmentCleanButtonClickCount() {
        int count = getJunkFragmentCleanButtonClickCount() + 1;
        getSharedPreferences().edit().putInt(PRES_JUNK_FRAGMENT_CLEAN_BUTTON_CLICK_COUNT, count).commit();
    }

    public static boolean canShowBatteryAlert() {
        int index = getBatteryAlertShowedCount() + 1;
        int seriesValue = getSeriesValue(index);
        int buttonClickTime = getBatteryOptimizeButtonClickCount();
        L.l("buttonClickTime=" + buttonClickTime + "  seriesValue: " + seriesValue + " index:" + index);
        return buttonClickTime == seriesValue;
    }

    public static int getBatteryAlertShowedTotalCount() {
        return getSharedPreferences().getInt(PREFS_BATTERY_SAVE_MODE_ALERT_SHOW_TOTAL_COUNT, 0);
    }

    public static int getBatteryAlertShowedCount() {
        return getSharedPreferences().getInt(PREFS_BATTERY_SAVE_MODE_ALERT_SHOW_COUNT, 0);
    }

    private static final int getSeriesValue(int index) {
        int result = index;
        if (index > 1) {
            result = getSeriesValue(index - 2) + getSeriesValue(index - 1);
        }
        return result;
    }

    public static long getLastCpuAbsTimeTotal() {
        return getSharedPreferences().getLong(PREFS_LAST_CPU_TIME_TOTAL, 0);
    }

    public static void setCpuAbsTimeTotal(long time) {
        getSharedPreferences().edit().putLong(PREFS_LAST_CPU_TIME_TOTAL, time).commit();
    }

    public static int getSessionIDOffset() {
        return getSharedPreferences().getInt(PREFS_SESSIONID_OFFSET, 0);
    }

    public static void setSessionIDOffset(int sessionid) {
        getSharedPreferences().edit().putInt(PREFS_SESSIONID_OFFSET, sessionid).commit();
    }

    public static SharedPreferences getSharedPreferences() {
        return HSApplication.getContext().getSharedPreferences("config", Context.MODE_PRIVATE);
    }

    public static void setHasRated() {
        getSharedPreferences().edit().putBoolean(PREFS_VERSION_RATE_FLAG + "_" + Utils.getVersionCode(), true).commit();
    }

    public static boolean hasRated() {
        return getSharedPreferences().getBoolean(PREFS_VERSION_RATE_FLAG + "_" + Utils.getVersionCode(), false);
    }

    public static void setCurrentSessionHasShowedRateAlert() {
        getSharedPreferences().edit().putBoolean(PREFS_RATE_ALERT_SHOWED_FLAG, true).commit();
        setShowRateAlertSessionID();
        int showedCount = getRateAlertShowedCount() + 1;
        getSharedPreferences().edit().putInt(PREFS_RATE_ALERT_SHOWED_COUNT, showedCount).commit();

    }

    public static int getRateAlertShowedCount() {
        return getSharedPreferences().getInt(PREFS_RATE_ALERT_SHOWED_COUNT, 0);
    }

    public static int getMemoryProtectAlertShowedCount() {
        return getSharedPreferences().getInt(PREFS_MEMORY_PROTECT_ALERT_SHOWED_COUNT, 0);
    }

    public static void setUsageAccessAlertShowedCount() {
        int count = PrefsUtils.getMemoryProtectAlertShowedCount() + 1;
        getSharedPreferences().edit().putInt(PREFS_MEMORY_PROTECT_ALERT_SHOWED_COUNT, count).commit();
    }

    private static final void setShowRateAlertSessionID() {
        getSharedPreferences().edit().putInt(PREFS_RATE_ALERT_SHOWED_SESSION_ID, HSSessionMgr.getCurrentSessionId()).commit();
    }

    public static int getLastShowRateAlertSessionID() {
        return getSharedPreferences().getInt(PREFS_RATE_ALERT_SHOWED_SESSION_ID, -1);
    }

    public static boolean currentSessionHasShowedRateAlert() {
        return getSharedPreferences().getBoolean(PREFS_RATE_ALERT_SHOWED_FLAG, false);
    }

    public static void resetHasShowedRateAlert() {
        getSharedPreferences().edit().remove(PREFS_RATE_ALERT_SHOWED_FLAG).commit();
    }

    public static void setHasSetShortcut() {
        getSharedPreferences().edit().putBoolean(PREFS_HAS_SET_SHORTCUT, true).commit();
    }

    public static boolean hasSetShortcut() {
        return getSharedPreferences().getBoolean(PREFS_HAS_SET_SHORTCUT, false);
    }

    public static long getLastSessionEndTime() {
        return getSharedPreferences().getLong(PREFS_LAST_SESSION_END_TIME, 0);
    }

    public static void setLastSessionEndTime(long time) {
        getSharedPreferences().edit().putLong(PREFS_LAST_SESSION_END_TIME, time).commit();
    }

    // ----------- memory clean
    public static boolean isLastMemoryCleanedExpired() {
        long lastTime = getSharedPreferences().getLong(PREFS_LAST_MEMORY_BOOSTED_TIME, 0);
        long time = System.currentTimeMillis() - lastTime;
        return time > MBConfig.CLEAN_MEMORY_EXPIRED_SECOND * 1000;
    }

    public static void setLastMemoryCleanedTime() {
        long cleanTime = System.currentTimeMillis();
        getSharedPreferences().edit().putLong(PREFS_LAST_MEMORY_BOOSTED_TIME, cleanTime).commit();
        MemoryPrefsManager.getInstance().setCleaned();
    }

    public static long getLastMemoryCleanedTime() {
        return getSharedPreferences().getLong(PREFS_LAST_MEMORY_BOOSTED_TIME, 0);
    }

    public static float getLastMemoryBoostedPercent() {
        return getSharedPreferences().getFloat(PREFS_MEMORY_BOOSTED_PERCENT, 0.1f);
    }

    public static void setLastMemoryBoostedPercent(float percent) {
        getSharedPreferences().edit().putFloat(PREFS_MEMORY_BOOSTED_PERCENT, percent).commit();
    }

    public static long getLastMemoryBoostedSize() {
        return getSharedPreferences().getLong(PREFS_MEMORY_BOOSTED_SIZE, 0);
    }

    public static void setLastMemoryBoostedSize(long size) {
        getSharedPreferences().edit().putLong(PREFS_MEMORY_BOOSTED_SIZE, size).commit();
    }

    // ------- cpu cool record
    public static int getCpuDecreaseTemps() {
        return getSharedPreferences().getInt(PREFS_CPU_DECREASE_TEMP, 0);
    }

    public static void setCpuDecreaseTemperatures(int decreaseTemps) {
        getSharedPreferences().edit().putInt(PREFS_CPU_DECREASE_TEMP, decreaseTemps).commit();
    }

    public static int getFakeCpuCoolTemps() {
        return getSharedPreferences().getInt(PREFS_CPU_COOL_DOWN_TEMP, 0);
    }

    public static void setFakeCpuCoolTemperatures(int coolTemps) {
        getSharedPreferences().edit().putInt(PREFS_CPU_COOL_DOWN_TEMP, coolTemps).commit();
    }

    public static int getCpuCooledAppNumber() {
        return getSharedPreferences().getInt(PREFS_CPU_COOLED_APP_NUMBER, 0);
    }

    public static void setCpuCooledAppNumber(int appNumber) {
        getSharedPreferences().edit().putInt(PREFS_CPU_COOLED_APP_NUMBER, appNumber).commit();
    }

    public static int getLastBatterySavedMins() {
        return getSharedPreferences().getInt(PREFS_BATTERY_SAVED_MINS, 0);
    }

    public static void setLastBatterySavedMins(int savedMins) {
        getSharedPreferences().edit().putInt(PREFS_BATTERY_SAVED_MINS, savedMins).commit();
    }


    private static List<String> convertStringToList(String listString) {
        List<String> list = new ArrayList<>();
        String[] array = listString.split(";");
        for (String str : array) {
            list.add(str);
        }
        return list;
    }

    public static void enableToolBar(boolean flag) {
        getSharedPreferences().edit().putBoolean(PREFS_NOTIFY_TOOLBAR, flag).commit();
    }

    public static boolean isToolBarEnable() {
        return getSharedPreferences().getBoolean(PREFS_NOTIFY_TOOLBAR, true);
    }

    public static void setTemperatureInF(boolean flag) {
        getSharedPreferences().edit().putBoolean(PREFS_TEMPERATURE_IN_F, flag).commit();
    }

    public static boolean isTemperatureInF() {
        boolean flag = getSharedPreferences().getBoolean(PREFS_TEMPERATURE_IN_F, true);
        return flag;
    }
}

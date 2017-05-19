package com.ihs.inputmethod.feature.boost.plus;

import com.honeycomb.launcher.model.LauncherFiles;
import com.honeycomb.launcher.util.PreferenceHelper;
import com.ihs.commons.utils.HSLog;

import java.util.Map;

public class BoostPlusUtils {

    private static final String PREF_KEY_TURN_ON_ACCESSIBILITY = "PREF_KEY_TURN_ON_ACCESSIBILITY";
    private static final String PREF_KEY_ACCESSIBILITY_NOTICE = "PREF_KEY_ACCESSIBILITY_NOTICE";
    private static final String PREF_KEY_NORMAL_CLEAN_TOAST = "PREF_KEY_NORMAL_CLEAN_TOAST";
    private static final String PREF_KEY_COUNT_ACTION_BUTTON_CLICK = "PREF_KEY_COUNT_ACTION_BUTTON_CLICK";
    private static final String PREF_KEY_CLEANED_APPS_MAP = "PREF_KEY_CLEANED_APPS_MAP";

    static boolean hasTurnOnAccessibilityDialogShowed() {
        return PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getBoolean(PREF_KEY_TURN_ON_ACCESSIBILITY, false);
    }

    static void setTurnOnAccessibilityDialogShowed() {
        PreferenceHelper.get(LauncherFiles.BOOST_PREFS).putBoolean(PREF_KEY_TURN_ON_ACCESSIBILITY, true);
    }

    static boolean isNormalCleanToasted() {
        return PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getBoolean(PREF_KEY_NORMAL_CLEAN_TOAST, false);
    }

    static void setNormalCleanToasted() {
        PreferenceHelper.get(LauncherFiles.BOOST_PREFS).putBoolean(PREF_KEY_NORMAL_CLEAN_TOAST, true);
    }

    private static boolean hasAccessibilityNoticeDialogShowed() {
        return PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getBoolean(PREF_KEY_ACCESSIBILITY_NOTICE, false);
    }

    static void setAccessibilityNoticeDialogShowed() {
        PreferenceHelper.get(LauncherFiles.BOOST_PREFS).putBoolean(PREF_KEY_ACCESSIBILITY_NOTICE, true);
    }

    static Map<String, String> getCleanedAppsMap() {
        return PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getMap(PREF_KEY_CLEANED_APPS_MAP);
    }

    static void setCleanedAppsMap(Map<String, String> map) {
        PreferenceHelper.get(LauncherFiles.BOOST_PREFS).addMap(PREF_KEY_CLEANED_APPS_MAP, map);
    }

    static void removeCleanedAppsMap(String key) {
        Map<String, String> currentMap = getCleanedAppsMap();
        if (null != currentMap && currentMap.containsKey(key)) {
            currentMap.remove(key);
        }
        PreferenceHelper.get(LauncherFiles.BOOST_PREFS).putMap(PREF_KEY_CLEANED_APPS_MAP, currentMap);
    }

    static boolean shouldShowAccessibilityNoticeDialog(int canCleanAppSize) {
        int clickCount = PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getInt(PREF_KEY_COUNT_ACTION_BUTTON_CLICK, 0);
        boolean hasAccessibilityNoticeDialogShowed = hasAccessibilityNoticeDialogShowed();
        HSLog.d(BoostPlusActivity.TAG, "shouldShowAccessibilityNoticeDialog canCleanAppSize = " + canCleanAppSize + " clickCount = " + clickCount + " hasAccessibilityNoticeDialogShowed = " + hasAccessibilityNoticeDialogShowed);
        return (!hasAccessibilityNoticeDialogShowed && clickCount == BoostPlusConstant.INVALID_COUNT_LIMIT_ACTION_BUTTON_CLICK && canCleanAppSize >= BoostPlusConstant.CAN_CLEAN_APP_SIZE_LIMIT);
    }

    static void setActionButtonClickCount(boolean isReset) {
        boolean hasTurnOnAccessibilityDialogShowed = hasTurnOnAccessibilityDialogShowed();
        HSLog.d(BoostPlusActivity.TAG, "setActionButtonClickCount isReset = " + isReset + " hasTurnOnAccessibilityDialogShowed = " + hasTurnOnAccessibilityDialogShowed);
        if (hasTurnOnAccessibilityDialogShowed) {
            int oldCount = 0;
            if (!isReset) {
                oldCount = PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getInt(PREF_KEY_COUNT_ACTION_BUTTON_CLICK, 0);
            }
            HSLog.d(BoostPlusActivity.TAG, "setActionButtonClickCount oldCount = " + oldCount);
            if (oldCount >= BoostPlusConstant.TURN_ON_ACCESSIBILITY_INTERVAL_COUNT) {
                PreferenceHelper.get(LauncherFiles.BOOST_PREFS).putInt(PREF_KEY_COUNT_ACTION_BUTTON_CLICK, BoostPlusConstant.INVALID_COUNT_LIMIT_ACTION_BUTTON_CLICK);
            } else {
                if (oldCount != BoostPlusConstant.INVALID_COUNT_LIMIT_ACTION_BUTTON_CLICK) {
                    PreferenceHelper.get(LauncherFiles.BOOST_PREFS).putInt(PREF_KEY_COUNT_ACTION_BUTTON_CLICK, oldCount + 1);
                }
            }
        }
    }

}

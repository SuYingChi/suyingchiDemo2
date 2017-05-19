package com.ihs.inputmethod.feature.boost;

import android.content.Context;

import com.honeycomb.launcher.boost.auto.AutoCleanService;
import com.honeycomb.launcher.boost.plus.BoostPlusSettingsActivity;
import com.honeycomb.launcher.compat.CompatUtils;
import com.honeycomb.launcher.util.LauncherConfig;
import com.ihs.commons.utils.HSPreferenceHelper;

public class BoostUtils {

    private static final String PREF_KEY_BOOST_PLUS_ONCE_FOREVER = "boost_plus_last_once_forever";

    public static boolean shouldEnableBoostPlusConfig() {
        return LauncherConfig.getVariantBoolean("Application", "BoostPlus", "AutoCleanFeatureEnabled");
    }

    public static boolean shouldEnableBoostPlusFeature() {
        return shouldEnableBoostPlusConfig() && isDeviceSupportAutoClean();
    }

    private static boolean isDeviceSupportAutoClean() {
        return !CompatUtils.IS_XIAOMI_DEVICE;
    }

    public static void updateConfigOnFirstLaunch() {
        //AB TEST CODE for auto clean.
//        boolean isFirstLaunch = HSVersionControlUtils.isFirstLaunchSinceInstallation();
//        if (isFirstLaunch) {
//            HSAnalytics.logEvent("App_FirstStart_AutoClean_" + (shouldEnableBoostPlusConfig() ? "Open" : "Closed"));
//        }
//        if (isFirstLaunch && shouldEnableBoostPlusConfig()) {
//            HSLog.d("BoostUtil", "Enable boost plus forever(Except clear data, upgrade)");
//            HSAnalytics.logEvent("Boost_Plus_Auto_Boost_Switch_On");
//            PreferenceHelper.get(LauncherFiles.BOOST_PREFS).putBoolean(PREF_KEY_BOOST_PLUS_ONCE_FOREVER, true);
//        }
    }

    public static void restartAutoCleanIfNeeded(Context applicationContext) {
        boolean autoBoostEnabled = HSPreferenceHelper.getDefault().getBoolean(BoostPlusSettingsActivity.PREF_KEY_AUTO_BOOST_ENABLED, false);
        if (autoBoostEnabled) {
            AutoCleanService.restart(applicationContext);
        }
    }
}

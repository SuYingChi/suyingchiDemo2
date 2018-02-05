package com.ihs.inputmethod.uimodules.utils;

import com.ihs.app.utils.HSVersionControlUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;

public class HSAppLockerUtils {
    private static final String SP_ENABLE_LOCKER = "SP_ENABLE_LOCKER";

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static boolean isLockerEnabled() {
//        boolean lockerEnable = HSConfig.optBoolean(false, "Application", "AppLocker", "Enable");
//        HSPreferenceHelper spHelper = HSPreferenceHelper.getDefault();
//
//        if (lockerEnable) {
//            spHelper.putBoolean(SP_ENABLE_LOCKER, true);
//            return true;
//        } else {
//            if (HSVersionControlUtils.isFirstSessionSinceInstallation()) {
//                spHelper.putBoolean(SP_ENABLE_LOCKER, false);
//                return false;
//            } else {
//                return spHelper.getBoolean(SP_ENABLE_LOCKER, true);
//            }
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
}

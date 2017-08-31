package com.ihs.inputmethod.utils;

import com.acb.call.CPSettings;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

/**
 * Created by yanxia on 2017/8/31.
 */

public class CallAssistantConfigUtils {
    private static final String PREF_KEY_CALL_ASSISTANT_ALERT_SHOW_TIME = "pref_call_assistant_alert_show_time";
    private static final int MAX_SHOW_COUNT = 2;

    public static boolean shouldShowCallAssistantAlert(boolean limitShowCount) {
        if (CPSettings.isScreenFlashModuleEnabled()) {
            return false;
        }
        if (CPSettings.isFunctionEnabledBefore()) {
            return false;
        }
        if (limitShowCount) {
            if (isEnableAlertShowCountAchievedMax()) {
                HSLog.i("Call Assistant alert achieved max show count");
                return false;
            }
        }
        return true;
    }

    private static boolean isEnableAlertShowCountAchievedMax() {
        return HSPreferenceHelper.getDefault(HSApplication.getContext()).getInt(PREF_KEY_CALL_ASSISTANT_ALERT_SHOW_TIME, 0) >= MAX_SHOW_COUNT;
    }

    public static void increaseAlertShowCount() {
        int showCount = HSPreferenceHelper.getDefault(HSApplication.getContext()).getInt(PREF_KEY_CALL_ASSISTANT_ALERT_SHOW_TIME, 0);
        showCount++;
        HSPreferenceHelper.getDefault(HSApplication.getContext()).putInt(PREF_KEY_CALL_ASSISTANT_ALERT_SHOW_TIME, showCount);
    }
}

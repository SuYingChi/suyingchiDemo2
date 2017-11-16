package com.ihs.inputmethod.utils;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.text.TextUtils;
import android.view.View;

import com.acb.call.CPSettings;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.charging.ChargingConfigManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.widget.CustomDesignAlert;

/**
 * Created by yanxia on 2017/8/29.
 */

public class TrialKeyboardDialogAlertUtils {

    private static final String TAG_CHARGING = "charging";
    private static final String TAG_CALL_ASSISTANT = "call_assistant";

    private static final String PREF_KEY_LAST_FUNCTION_TAG = "pref_last_function_tag";
    private static final String PREF_KEY_ALERT_SHOW_TIME = "special_function_alert_show_time";

    private static final int MAX_SHOW_COUNT = 6;

    public static void showSpecialFunctionEnableAlert() {
        if (reachMaxShowCount()) {
            return;
        }
        if (TextUtils.equals(TAG_CALL_ASSISTANT, getLastShowFunctionTag())) {
            if (!showChargingAlert()) {
                showCallAssistantAlert();
            }
        } else if (TextUtils.equals(TAG_CHARGING, getLastShowFunctionTag())) {
            if (!showCallAssistantAlert()) {
                showChargingAlert();
            }
        } else {
            HSLog.d("do nothing.");
        }
    }

    private static boolean reachMaxShowCount() {
        int showCount = HSPreferenceHelper.getDefault(HSApplication.getContext()).getInt(PREF_KEY_ALERT_SHOW_TIME, 0);
        return showCount >= MAX_SHOW_COUNT;
    }

    private static String getLastShowFunctionTag() {
        return HSPreferenceHelper.getDefault(HSApplication.getContext()).getString(PREF_KEY_LAST_FUNCTION_TAG, TAG_CALL_ASSISTANT);
    }

    private static void setLastShowFunctionTag(String tag) {
        HSPreferenceHelper.getDefault().putString(PREF_KEY_LAST_FUNCTION_TAG, tag);
    }

    private static boolean showChargingAlert() {
        if (ChargingConfigManager.getManager().shouldShowEnableChargingAlert(false)) {
            CustomDesignAlert dialog = new CustomDesignAlert(HSApplication.getContext());
            dialog.setTitle(HSApplication.getContext().getString(R.string.charging_alert_title));
            dialog.setMessage(HSApplication.getContext().getString(R.string.charging_alert_message));
            dialog.setImageResource(R.drawable.enable_charging_alert_top_image);
            dialog.setCancelable(true);
            dialog.setPositiveButton(HSApplication.getContext().getString(R.string.enable), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChargingManagerUtil.enableCharging(false);
                    HSAnalytics.logEvent("alert_charging_click", "size", "half_screen", "occasion", "after_try");
                }
            });

            dialog.show();
            HSAnalytics.logEvent("alert_charging_show", "size", "half_screen", "occasion", "after_try");
            increaseAlertShowCount();
            setLastShowFunctionTag(TAG_CHARGING);
            return true;
        } else {
            return false;
        }
    }

    private static boolean showCallAssistantAlert() {
        if (CallAssistantConfigUtils.shouldShowCallAssistantAlert(false)) {
            CustomDesignAlert dialog = new CustomDesignAlert(HSApplication.getContext());
            dialog.setTitle(HSApplication.getContext().getString(R.string.call_assistant_alert_title));
            dialog.setMessage(HSApplication.getContext().getString(R.string.call_assistant_alert_message));
            dialog.setImageResource(R.drawable.enable_tripple_alert_top_image);//call image
            dialog.setCancelable(true);
            dialog.setEnablePrivacy(true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startBrowsePrivacy();
                }
            });
            dialog.setPositiveButton(HSApplication.getContext().getString(R.string.enable), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HSAnalytics.logEvent("alert_call_assistant_click", "size", "half_screen", "occasion", "after_try");
                    CPSettings.setCallAssistantModuleEnabled(true);
                }
            });
            dialog.show();
            HSAnalytics.logEvent("alert_call_assistant_show", "size", "half_screen", "occasion", "after_try");
            increaseAlertShowCount();
            setLastShowFunctionTag(TAG_CALL_ASSISTANT);
            return true;
        } else {
            return false;
        }
    }

    private static void startBrowsePrivacy() {
        Uri uri = Uri.parse(HSConfig.optString("", "Application", "Policy", "PrivacyPolicy"));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, HSApplication.getContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            HSApplication.getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            HSLog.w("URLSpan", "Activity was not found for intent, " + intent.toString());
        }
    }

    private static void increaseAlertShowCount() {
        int showCount = HSPreferenceHelper.getDefault(HSApplication.getContext()).getInt(PREF_KEY_ALERT_SHOW_TIME, 0);
        showCount++;
        HSPreferenceHelper.getDefault(HSApplication.getContext()).putInt(PREF_KEY_ALERT_SHOW_TIME, showCount);
    }
}

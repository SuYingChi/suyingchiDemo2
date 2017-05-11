package com.ihs.inputmethod.utils;

import android.content.DialogInterface;
import android.view.View;

import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.keyboardutils.alerts.CustomUIRateAlert;

import java.util.List;

public class CustomUIRateAlertUtils {
    private static final String TAG = CustomUIRateAlertUtils.class.getSimpleName();
    public static String title;
    public static String message;
    private static List<String> actionTextList;
    private static List<DialogInterface.OnClickListener> actionListenerList;

    public static void initialize() {
        HSAlertMgr.setShowCustomUIForAlert(new HSAlertMgr.IShowCustomUIForAlert() {
            @Override
            public boolean showCustomUIForAlert(String s, String s1, String s2, List<String> list, List<DialogInterface.OnClickListener> list1) {
                HSLog.d(TAG, "alert: " + s + ", title: " +  s1 + ", message: " + s2 + ", buttons: " + list.toString() + ", callbacks: " + list1.toString());
                if (s.equals(HSAlertMgr.RATE_ALERT)) {
                    showRateAlert(s1, s2, list, list1);
                    return true;
                }

                return false;
            }
        });
    }

    private static String getPositiveButtonText() {
        if (actionTextList != null && !actionTextList.isEmpty()) {
            HSLog.d(TAG, actionTextList.toString());
            return actionTextList.get(0);
        }

        return "";
    }

    private static String getNegativeButtonText() {
        if (actionTextList != null && !actionTextList.isEmpty() && actionTextList.size() > 1) {
            HSLog.d(TAG, actionTextList.toString());
            return actionTextList.get(1);
        }

        return "";
    }

    /**
     * Rate
     * @param dialog
     */
    private static void onPositiveButtonClick(DialogInterface dialog) {
        if (actionListenerList != null && !actionListenerList.isEmpty()) {
            actionListenerList.get(0).onClick(dialog, 0);
        }

        HSGoogleAnalyticsUtils.getInstance().logAppEvent("custom_theme_simulate_rate_ratenow_clicked");
    }

    /**
     * No thanks
     * @param dialog
     */
    private static void onNegativeButtonClick(DialogInterface dialog) {
        if (actionListenerList != null && !actionListenerList.isEmpty() && actionListenerList.size() > 1) {
            actionListenerList.get(1).onClick(dialog, 1);
        }

        HSGoogleAnalyticsUtils.getInstance().logAppEvent("custom_theme_simulate_rate_later_clicked");
    }

    /**
     * Dislike
     * @param dialog
     */
    private static void onNeutralButtonClick(DialogInterface dialog) {
        if (actionListenerList != null && !actionListenerList.isEmpty() && actionListenerList.size() > 2) {
            actionListenerList.get(2).onClick(dialog, 2);
        }
    }

    private static void showRateAlert(String s1, String s2, List<String> list1, List<DialogInterface.OnClickListener> list2) {
        updateRateAlertInfo(s1, s2, list1, list2);

        final CustomUIRateAlert dialog = new CustomUIRateAlert(HSApplication.getContext());

        dialog.setPositiveButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPositiveButtonClick(dialog);
            }
        });
        dialog.setNegativeButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNegativeButtonClick(dialog);
            }
        });
        dialog.setNeutralButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNeutralButtonClick(dialog);
            }
        });

        dialog.show();

        HSGoogleAnalyticsUtils.getInstance().logAppEvent("custom_theme_simulate_rate_showed");
    }

    private static void updateRateAlertInfo(String s1, String s2, List<String> list1, List<DialogInterface.OnClickListener> list2) {
        title = s1;
        message = s2;
        actionTextList = list1;
        actionListenerList = list2;
    }
}

package com.ihs.inputmethod.feature.customuiratealert;

import android.content.DialogInterface;
import android.view.View;

import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.widget.CustomDesignAlert;

import java.util.List;

public class CustomUIRateAlertManager {
    private static final String TAG = CustomUIRateAlertManager.class.getSimpleName();
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

    static String getPositiveButtonText() {
        HSLog.d(TAG, actionTextList.toString());
        if (actionTextList != null && !actionTextList.isEmpty()) {
            return actionTextList.get(0);
        }

        return "";
    }

    static String getNegativeButtonText() {
        HSLog.d(TAG, actionTextList.toString());
        if (actionTextList != null && !actionTextList.isEmpty() && actionTextList.size() > 1) {
            return actionTextList.get(1);
        }

        return "";
    }

    static void onPositiveButtonClick(DialogInterface dialog) {
        if (CustomUIRateAlertManager.actionListenerList != null && !CustomUIRateAlertManager.actionListenerList.isEmpty()) {
            CustomUIRateAlertManager.actionListenerList.get(0).onClick(dialog, 0);
        }
    }

    static void onNegativeButtonClick(DialogInterface dialog) {
        if (CustomUIRateAlertManager.actionListenerList != null && !CustomUIRateAlertManager.actionListenerList.isEmpty() && CustomUIRateAlertManager.actionListenerList.size() > 1) {
            CustomUIRateAlertManager.actionListenerList.get(1).onClick(dialog, 1);
        }
    }

    private static void showRateAlert(String s1, String s2, List<String> list1, List<DialogInterface.OnClickListener> list2) {
        updateRateAlertInfo(s1, s2, list1, list2);

        final CustomDesignAlert dialog = new CustomDesignAlert(HSApplication.getContext());
        dialog.setCancelable(false);
        dialog.setTitle(CustomUIRateAlertManager.title);
        dialog.setMessage(CustomUIRateAlertManager.message);
        dialog.setImageResource(R.drawable.rate_alert_top_bg);
        dialog.setPositiveButton(getPositiveButtonText(), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomUIRateAlertManager.onPositiveButtonClick(dialog);
            }
        });
        dialog.setNegativeButton(getNegativeButtonText(), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomUIRateAlertManager.onNegativeButtonClick(dialog);
            }
        });

        dialog.show();
    }

    private static void updateRateAlertInfo(String s1, String s2, List<String> list1, List<DialogInterface.OnClickListener> list2) {
        title = s1;
        message = s2;
        actionTextList = list1;
        actionListenerList = list2;
    }

    static void clearRateAlertInfo() {
        updateRateAlertInfo(null, null, null, null);
    }
}

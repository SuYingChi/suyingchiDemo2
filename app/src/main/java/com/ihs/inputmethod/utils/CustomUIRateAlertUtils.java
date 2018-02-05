package com.ihs.inputmethod.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;

import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.alerts.CustomUIRateAlert;
import com.ihs.keyboardutils.alerts.CustomUIRateBaseAlert;
import com.ihs.keyboardutils.alerts.CustomUIRateOneAlert;
import com.ihs.keyboardutils.alerts.CustomUIRateThreeAlert;
import com.ihs.keyboardutils.alerts.CustomUIRateTwoAlert;
import com.kc.commons.utils.KCCommonUtils;

import java.util.List;

public class CustomUIRateAlertUtils {
    private static final String TAG = CustomUIRateAlertUtils.class.getSimpleName();
    // --Commented out by Inspection (18/1/11 下午2:41):public static String title;
    // --Commented out by Inspection (18/1/11 下午2:41):public static String message;
    private static List<String> actionTextList;
    private static List<DialogInterface.OnClickListener> actionListenerList;
    final private static String RATE_ALERT_TYPE_DEFAULT = "0";
    final private static String RATE_ALERT_TYPE_ONE = "1";
    final private static String RATE_ALERT_TYPE_TWO = "2";
    final private static String RATE_ALERT_TYPE_THREE = "3";

    public static void initialize() {
        HSAlertMgr.setShowCustomUIForAlert(new HSAlertMgr.IShowCustomUIForAlert() {
            @Override
            public boolean showCustomUIForAlert(Activity activity, String s, String s1, String s2, List<String> list, List<DialogInterface.OnClickListener> list1) {
                HSLog.d(TAG, "alert: " + s + ", title: " +  s1 + ", message: " + s2 + ", buttons: " + list.toString() + ", callbacks: " + list1.toString());
                if (s.equals(HSAlertMgr.RATE_ALERT)) {
                    showRateAlert(activity,s1, s2, list, list1);
                    return true;
                }

                return false;
            }
        });
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    private static String getPositiveButtonText() {
//        if (actionTextList != null && !actionTextList.isEmpty()) {
//            HSLog.d(TAG, actionTextList.toString());
//            return actionTextList.get(0);
//        }
//
//        return "";
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    private static String getNegativeButtonText() {
//        if (actionTextList != null && !actionTextList.isEmpty() && actionTextList.size() > 1) {
//            HSLog.d(TAG, actionTextList.toString());
//            return actionTextList.get(1);
//        }
//
//        return "";
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    /**
     * Rate
     * @param dialog
     */
    private static void onPositiveButtonClick(DialogInterface dialog) {
        if (actionListenerList != null && !actionListenerList.isEmpty()) {
            actionListenerList.get(0).onClick(dialog, 0);
        }
    }

    /**
     * No thanks
     * @param dialog
     */
    private static void onNegativeButtonClick(DialogInterface dialog) {
        if (actionListenerList != null && !actionListenerList.isEmpty() && actionListenerList.size() > 1) {
            actionListenerList.get(1).onClick(dialog, 1);
        }
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

    private static void onDismissClick(DialogInterface dialog) {
        if (actionListenerList != null && !actionListenerList.isEmpty()) {
            actionListenerList.get(1).onClick(dialog, 1);
        }
    }

    private static void showRateAlert(Activity activity,String s1, String s2, List<String> list1, List<DialogInterface.OnClickListener> list2) {
        updateRateAlertInfo(s1, s2, list1, list2);

        String rateAlertType = HSConfig.optString("0", "Application", "RateAlert", "CurrentType");

        final CustomUIRateBaseAlert dialog;

        switch (rateAlertType) {
            case RATE_ALERT_TYPE_DEFAULT:
                dialog = new CustomUIRateAlert(activity);

                ((CustomUIRateAlert)dialog).setPositiveButtonOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onPositiveButtonClick(dialog);
                    }
                });
                ((CustomUIRateAlert)dialog).setNegativeButtonOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onNegativeButtonClick(dialog);
                    }
                });
                ((CustomUIRateAlert)dialog).setNeutralButtonOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onNeutralButtonClick(dialog);
                    }
                });
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        onNegativeButtonClick(dialog);
                    }
                });
                break;
            case RATE_ALERT_TYPE_ONE:
                dialog = new CustomUIRateOneAlert(activity);
                dialog.setDismissListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDismissClick(dialog);
                    }
                });
                break;
            case RATE_ALERT_TYPE_TWO:
                dialog = new CustomUIRateTwoAlert(activity);
                dialog.setDismissListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDismissClick(dialog);
                    }
                });
                break;
            case RATE_ALERT_TYPE_THREE:
                dialog = new CustomUIRateThreeAlert(activity);
                dialog.setDismissListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDismissClick(dialog);
                    }
                });
                break;
            default:
                dialog = new CustomUIRateAlert(activity);

                ((CustomUIRateAlert)dialog).setPositiveButtonOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onPositiveButtonClick(dialog);
                    }
                });
                ((CustomUIRateAlert)dialog).setNegativeButtonOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onNegativeButtonClick(dialog);
                    }
                });
                ((CustomUIRateAlert)dialog).setNeutralButtonOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onNeutralButtonClick(dialog);
                    }
                });
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        onNegativeButtonClick(dialog);
                    }
                });
                break;
        }

        KCCommonUtils.showDialog(dialog);
    }

    private static void updateRateAlertInfo(String s1, String s2, List<String> list1, List<DialogInterface.OnClickListener> list2) {
        actionTextList = list1;
        actionListenerList = list2;
    }
}

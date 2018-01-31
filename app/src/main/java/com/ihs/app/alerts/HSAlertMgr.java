package com.ihs.app.alerts;

import android.app.Activity;
import android.content.DialogInterface;

import java.util.List;

/**
 * Created by Arthur on 2018/1/31.
 */

public class HSAlertMgr {
    public static final String RATE_ALERT = "aa";
    private static IShowCustomUIForAlert showCustomUIForAlert;

    public static void delayRateAlert() {

    }

    public static boolean isAlertShown() {
        return false;
    }

    public static void showRateAlert() {

    }

    public static void setShowCustomUIForAlert(IShowCustomUIForAlert showCustomUIForAlert) {
        HSAlertMgr.showCustomUIForAlert = showCustomUIForAlert;
    }

    public interface IShowCustomUIForAlert {
        public boolean showCustomUIForAlert(Activity activity, String s, String s1, String s2, List<String> list, List<DialogInterface.OnClickListener> list1) ;
    }
}

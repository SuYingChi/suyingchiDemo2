package com.ihs.inputmethod.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.keyboardutils.notification.KCNotificationManager;
import com.ihs.keyboardutils.utils.KCAnalyticUtil;

/**
 * Created by Arthur on 17/5/6.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    public static final String INTENT_NOTIFICATION = "com.ihs.intent.notification";

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intent1 = new Intent(context, ThemeHomeActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        HSLog.e(intent.getStringExtra("eventName") + intent.toString());
        KCAnalyticUtil.logEvent("notification_click",intent.getStringExtra("eventName"));
        switch (intent.getStringExtra("eventName")) {
            case "Charging":
                ChargingManagerUtil.enableCharging(true,"notification");
                KCNotificationManager.getInstance().removeNotificationEvent("Charging");
                HSToastUtils.toastBottomShort("Fast Charging Enabled");
                break;
            case "SetPhotoAsBackground":
            case "ChangeTheme":
            case "ChangeFont":
                context.startActivity(intent1);
                IAPManager.startCustomThemeActivity(null);
                break;
        }


    }
}

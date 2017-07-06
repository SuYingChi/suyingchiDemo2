package com.ihs.inputmethod.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artw.lockscreen.LockerSettings;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeDetailActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
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

        HSLog.e(intent.getStringExtra("actionType") + intent.toString());
        KCAnalyticUtil.logEvent("notification_click",intent.getStringExtra("eventName"));
        String name = intent.getStringExtra("name");
        HSLog.e("notification name " + name);
        switch (intent.getStringExtra("actionType")) {
            case "Charging":
                ChargingManagerUtil.enableCharging(false,"notification");
                HSToastUtils.toastBottomShort("Fast Charging Enabled");
                break;
            case "SetPhotoAsBackground":
            case "CustomizedTheme":
            case "ChangeFont":
                context.startActivity(intent1);
                CustomThemeActivity.startCustomThemeActivity(null);
                break;
            case "Theme":
                context.startActivity(intent1);
                Intent intent2 = new Intent(context, ThemeDetailActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra(ThemeDetailActivity.INTENT_KEY_THEME_NAME, name);
                context.startActivity(intent2);
                break;
            case "Locker":
                LockerSettings.setLockerEnabled(true,"notification");
                break;
        }
    }
}

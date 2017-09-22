package com.ihs.inputmethod.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artw.lockscreen.LockerSettings;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.theme.download.ThemeDownloadManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.keyboardutils.notification.KCNotificationManager;

import java.util.List;

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
        HSAnalytics.logEvent("notification_click","notification_click",intent.getStringExtra("name"));
        KCNotificationManager.logNotificationClick(intent.getStringExtra("actionType"),intent.getStringExtra("name"));
        String name = intent.getStringExtra("name");
        HSLog.e("notification name " + name);
        switch (intent.getStringExtra("actionType")) {
            case "Charging":
                ChargingManagerUtil.enableCharging(true);
                HSToastUtils.toastBottomShort("Fast Charging Enabled");
                break;
            case "SetPhotoAsBackground":
            case "CustomizedTheme":
            case "ChangeFont":
                context.startActivity(intent1);
                CustomThemeActivity.startCustomThemeActivity(null);
                break;
            case "Theme":
                List<HSKeyboardTheme> allKeyboardThemeList = HSKeyboardThemeManager.getAllKeyboardThemeList();
                for (HSKeyboardTheme keyboardTheme : allKeyboardThemeList) {
                    if (name.equals(keyboardTheme.mThemeName) || name.equals(keyboardTheme.getThemePkName())) {
                        ThemeDownloadManager.getInstance().downloadTheme(keyboardTheme);
                        break;
                    }
                }
                break;
            case "Locker":
                LockerSettings.setLockerEnabled(true);
                break;
        }
    }
}

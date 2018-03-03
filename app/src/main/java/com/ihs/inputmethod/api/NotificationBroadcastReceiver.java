package com.ihs.inputmethod.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artw.lockscreen.LockerSettings;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.theme.download.ThemeDownloadManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.keyboardutils.notification.KCNotificationManager;
import com.kc.utils.KCAnalytics;

import java.util.List;

/**
 * Created by Arthur on 17/5/6.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    // --Commented out by Inspection (18/1/11 下午2:41):public static final String INTENT_NOTIFICATION = "com.ihs.intent.notification";

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Intent intent1 = new Intent(context, Class.forName(HSApplication.getContext().getString(R.string.home_activity_name)));
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            HSLog.e(intent.getStringExtra("actionType") + intent.toString());
            KCAnalytics.logEvent("notification_click","notification_click",intent.getStringExtra("name"));
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
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

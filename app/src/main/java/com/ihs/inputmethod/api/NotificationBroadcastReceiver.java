package com.ihs.inputmethod.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;

/**
 * Created by Arthur on 17/5/6.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    public static final String INTENT_NOTIFICATION = "com.ihs.intent.notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, ThemeHomeActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);
        
        HSLog.e(intent.getStringExtra("eventName") + intent.toString());
        switch (intent.getStringExtra("eventName")) {
            case "Charging":
                HSUIInputMethod.launchSettingsActivity();
                break;
            case "SetPhotoAsBackground":
            case "ChangeTheme":
            case "ChangeFont":
                IAPManager.startCustomThemeActivity(null);
                break;
        }


    }
}

package com.ihs.inputmethod.api;

import android.content.Intent;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSActivityUtils;

public class HSUIInputMethod {
    public static final String HS_LAUNCH_LANGUAGE_SETTINGS="LAUNCH_LANGUAGE_SETTINGS";
    public static final String HS_LAUNCH_SETTINGS="LAUNCH_SETTINGS";

    public static void launchMoreLanguageActivity() {
        final Intent intent = new Intent();
        intent.setAction(HSApplication.getContext().getPackageName()+"."+HS_LAUNCH_LANGUAGE_SETTINGS);
        if (HSActivityUtils.isAppActivityOnTop()) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        HSApplication.getContext().startActivity(intent);
    }


    public static void launchSettingsActivity() {
        final Intent intent = new Intent();
        intent.setAction(HSApplication.getContext().getPackageName()+"."+HS_LAUNCH_SETTINGS);
        if (HSActivityUtils.isAppActivityOnTop()) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        HSApplication.getContext().startActivity(intent);
    }
}

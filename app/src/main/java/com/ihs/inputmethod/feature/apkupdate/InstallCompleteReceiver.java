package com.ihs.inputmethod.feature.apkupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;

public class InstallCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getDataString();
        HSLog.d("Receive intent,data:" + name);

        // Delete apk file
        ApkDownloadManager.getInstance().removeLastDownloadFile();

        // HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("app_apk_download_install_success");

        // Restart app only it download by sp channel.
//        if (BuildConfig.FLAVOR.equals(LauncherConstants.BUILD_VARIANT_SP)) {
//            CommonUtils.startLauncher(context);
//        }
    }
}

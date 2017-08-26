package com.ihs.inputmethod.feature.apkupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ihs.commons.utils.HSLog;

public class InstallCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getDataString();
        HSLog.d("Receive intent,data:" + name);

        // Delete apk file
        ApkDownloadManager.getInstance().removeLastDownloadFile();
    }
}

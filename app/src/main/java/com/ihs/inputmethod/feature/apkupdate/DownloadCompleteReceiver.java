package com.ihs.inputmethod.feature.apkupdate;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;

/**
 * When apk download completes, this receiver handles it.
 */
public class DownloadCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        HSLog.d("downloadId: " + downloadId);

        if (downloadId > 0) {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = downloadManager.getUriForDownloadedFile(downloadId);

            long myId = ApkDownloadManager.obtainDownloadId(UpdateConfig.getDefault());
            if (myId == downloadId) {
                // launcher App update
                HSLog.d("Download update apk succeed: " + uri);
            } else {
                HSLog.e("Download update apk failed(Non-expected downloadId: " + myId+")");
            }

            context.unregisterReceiver(this);

            HSLog.d("Install apk:" + uri);

            if (uri != null) {
                try { //Galaxy Tab S2 (171号手机会挂，先try catch处理)
                    ApkUtils.startInstall(context, uri);
                }catch (Exception e){

                }
            }
        }
    }
}

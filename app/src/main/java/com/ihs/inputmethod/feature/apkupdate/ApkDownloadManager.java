package com.ihs.inputmethod.feature.apkupdate;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

public class ApkDownloadManager {

    public static long STATUS_INVALID_URL = -100;
    public static long STATUS_TO_INSTALL = -99;
    public static long STATUS_DOWNLOADING = -98;
    public static long STATUS_INVALID_SETTINGS = -97;

    private static final String PREF_KEY_APK_ID = "ApkDownloadManager.apk_download_id";
    private static final String PREF_KEY_APK_TIME = "ApkDownloadManager.apk_download_time";

    private static ApkDownloadManager INSTANCE;

    private final DownloadCompleteReceiver mDownloadCompleteReceiver;
    private DownloadManager mDownloadManager;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ApkDownloadManager() {
        mDownloadManager = (DownloadManager) HSApplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        mDownloadCompleteReceiver = new DownloadCompleteReceiver();
    }

    public static ApkDownloadManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ApkDownloadManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ApkDownloadManager();
                }
            }
        }
        return INSTANCE;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * Entrance.
//     */
//    synchronized long checkDownload(UpdateConfig config) {
//        if (config == null || TextUtils.isEmpty(config.getDownLoadUrl())){
//            return STATUS_INVALID_URL;
//        }
//        // Find apk in local storage
//        boolean isFileReady = false;
//        File localFile = null;
//        try {
//            File file = HSApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
//            localFile = new File(file, config.getLocalFileName());
//            if (config.isUpdateMode()) {
//                // Is latest Apk file.
//                isFileReady = ApkUtils.isLocalApkReady(localFile.getPath(), HSApplication.getContext());
//            } else {
//                // Is Apk file.
//                isFileReady = ApkUtils.getApkInfo(HSApplication.getContext(), localFile.getPath()) != null;
//            }
//        }  catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (isFileReady) {
//            ApkUtils.startInstall(HSApplication.getContext(), Uri.fromFile(localFile));
//            return STATUS_TO_INSTALL;
//        }
//
//        // Check download id
//        final long lastDownLoadId = obtainDownloadId(config);
//        if (lastDownLoadId > 0) {
//            int downloadStatus = ApkUtils.getDownloadStatus(mDownloadManager, lastDownLoadId);
//            if (downloadStatus == DownloadManager.STATUS_RUNNING || downloadStatus == DownloadManager.STATUS_PAUSED) {
//               // Is downloading
//                if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            HSToastUtils.toastCenterLong(HSApplication.getContext().getString(R.string.apk_update_downloading));
//                        }
//                    });
//                } else {
//                    HSToastUtils.toastCenterLong(HSApplication.getContext().getString(R.string.apk_update_downloading));
//                }
//                return STATUS_DOWNLOADING;
//            } else if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
//                Uri uri = mDownloadManager.getUriForDownloadedFile(lastDownLoadId);
//                // Local apk file is latest.
//                // User may delete the file, uri may be null
//                if (uri != null && ApkUtils.isLocalApkReady(uri.getPath(), HSApplication.getContext())) {
//                    ApkUtils.startInstall(HSApplication.getContext(), uri);
//                    return STATUS_TO_INSTALL;
//                }
//            }
//
//            // Delete old apk file
//            // When download error and Failed, start over again.
//            removeDownloadFile(lastDownLoadId);
//        }
//
//        if (!ApkUtils.isDownloadManagerReady(HSApplication.getContext())) {
//            showSettingForDownloadManager();
//            return STATUS_INVALID_SETTINGS;
//        }
//
//        preDownloadApk();
//
//        long downLoadId = startDownloadApk(config);
//        saveId(config, downLoadId);
//        return downLoadId;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * Calc elapsed time after last file start download
//     * @return 0 if no download file , otherwise the real time.
//     */
//    public long getTimeMillsAfterDownload() {
//        if (obtainDownloadId(UpdateConfig.getDefault()) > 0) {
//            final long now = SystemClock.elapsedRealtime();
//            final long last = HSPreferenceHelper.getDefault().getLong(PREF_KEY_APK_TIME, now);
//            return now - last;
//        }
//        return 0;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    /**
     * Remove last download file.
     * @return
     */
    public boolean removeLastDownloadFile() {
        UpdateConfig config = UpdateConfig.getDefault();
        long id = obtainDownloadId(config);
        HSPreferenceHelper.getDefault().putLong(getPrefKey(config), -1);

        return  removeDownloadFile(id);
    }

    private boolean removeDownloadFile(long id) {
        HSLog.d("try remove download file ,id = " + id);
        if (ApkUtils.isDownloadManagerReady(HSApplication.getContext()) && id > 0) {
            HSLog.d("Removed download file ,id = " + id);
            return mDownloadManager.remove(id) > 0;
        }
        return false;
    }

    /**
     * Get last download id, this for app update.
     * @deprecated use {@link #obtainDownloadId(UpdateConfig)}
     * @return id
     */
    long obtainId() {
        return obtainDownloadId(null);
    }

    /**
     *  Get download Id
     * @param config
     * @return -1 if not exist. or id from {@link DownloadManager#enqueue(DownloadManager.Request)}
     */
    public static long obtainDownloadId(UpdateConfig config) {
        String key = getPrefKey(config);
        return HSPreferenceHelper.getDefault().getLong(key, -1);
    }

    private static String getPrefKey(UpdateConfig config) {
        return (config == null || TextUtils.isEmpty(config.getMD5Key())) ? PREF_KEY_APK_ID : config.getMD5Key();
    }

    private void saveId(UpdateConfig config, long downLoadId) {
        HSPreferenceHelper.getDefault().putLong(getPrefKey(config), downLoadId);
    }

    private void showSettingForDownloadManager() {
        String packName = "com.android.providers.downloads";
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PackageManager pm = HSApplication.getContext().getPackageManager();
        if (pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            HSApplication.getContext().startActivity(intent);
        }
    }

    private void preDownloadApk() {
        // Save time that apk start download.
        HSPreferenceHelper.getDefault().putLong(PREF_KEY_APK_TIME, SystemClock.elapsedRealtime());

        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        HSApplication.getContext().registerReceiver(mDownloadCompleteReceiver, intentFilter);
    }

    private long startDownloadApk(UpdateConfig config) {
        DownloadManager.Request request = buildRequestByConfig(config);
        return mDownloadManager.enqueue(request);
    }

    private DownloadManager.Request buildRequestByConfig(UpdateConfig config) {
        if (config == null || TextUtils.isEmpty(config.getDownLoadUrl())) {
            throw new IllegalArgumentException("ApkDownloadManager got invalid Config:" + config);
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(config.getDownLoadUrl()));
        request.setVisibleInDownloadsUi(config.isShowInDownloadUI());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setDestinationInExternalFilesDir(HSApplication.getContext(), Environment.DIRECTORY_DOWNLOADS, config.getLocalFileName());

        request.setTitle(config.getTitle());
        request.setDescription(config.getDescription());
        request.setMimeType("application/vnd.android.package-archive");
        request.setAllowedNetworkTypes(config.isDownloadOnlyWifi() ? DownloadManager.Request.NETWORK_WIFI : ~0);

        return request;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * Get download file path by id.
//     */
//    public String getDownloadFilePath(long id) {
//        DownloadManager.Query query = new DownloadManager.Query().setFilterById(id);
//        Cursor cursor = mDownloadManager.query(query);
//        if (cursor != null) {
//            try {
//                if (cursor.moveToFirst()) {
//                    return cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
//                }
//            } finally {
//                cursor.close();
//            }
//        }
//        return null;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
}

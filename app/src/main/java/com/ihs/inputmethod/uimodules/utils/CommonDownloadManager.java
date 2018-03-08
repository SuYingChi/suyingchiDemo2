package com.ihs.inputmethod.uimodules.utils;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.api.utils.HSZipUtils;
import com.ihs.inputmethod.uimodules.ui.sticker.DownloadItem;
import com.kc.utils.KCAnalytics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

/**
 * Created by yanxia on 2017/11/6.
 */

public class CommonDownloadManager {

    private Map<DownloadItem, HSHttpConnection> downloadingItems;
    private Map<DownloadItem, UnzipDownloadItemTask> uncompressingItems;
    private Map<DownloadItem, List<OnDownloadUpdateListener>> downloadItemListenerMap;
    private List<OnDownloadUpdateListener> downloadUpdateListeners;

    public interface OnDownloadUpdateListener {
        void onDownloadStart(DownloadItem downloadItem);

        void onDownloadProgressUpdate(DownloadItem downloadItem, float percent);

        void onDownloadSuccess(DownloadItem downloadItem, long downloadTime);

        void onDownloadFailure(DownloadItem downloadItem);

        void onUncompressSuccess(DownloadItem downloadItem);

        void onUncompressFailure(DownloadItem downloadItem);
    }

    private static CommonDownloadManager instance;

    private CommonDownloadManager() {
        downloadingItems = new HashMap<>();
        uncompressingItems = new HashMap<>();
        downloadItemListenerMap = new HashMap<>();
        downloadUpdateListeners = new ArrayList<>();
    }

    public static CommonDownloadManager getInstance() {
        if (instance == null) {
            synchronized (CommonDownloadManager.class) {
                if (instance == null) {
                    instance = new CommonDownloadManager();
                }
            }
        }
        return instance;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean downloadItem(@NonNull DownloadItem downloadItem, @Nullable OnDownloadUpdateListener listener) {
        HSLog.d("downloadItem url: " + downloadItem.getDownloadUrl());
        if (downloadItem.isDownloaded()) { //已下载
            HSLog.i("downloadItem already downloaded.");
            return false;
        }
        List<OnDownloadUpdateListener> onDownloadUpdateListeners;
        if (!downloadItemListenerMap.containsKey(downloadItem)) {
            onDownloadUpdateListeners = new ArrayList<>();
            if (listener != null) {
                onDownloadUpdateListeners.add(listener);
            }
            downloadItemListenerMap.put(downloadItem, onDownloadUpdateListeners);
        } else {
            onDownloadUpdateListeners = downloadItemListenerMap.get(downloadItem);
            if (listener != null) {
                onDownloadUpdateListeners.add(listener);
            }
        }
        if (!downloadingItems.keySet().contains(downloadItem) && !uncompressingItems.keySet().contains(downloadItem)) { //目前没在下载也没有在解压，则新建下载任务
            final long startTime = SystemClock.elapsedRealtime();
            HSHttpConnection connection = new HSHttpConnection(downloadItem.getDownloadUrl());
            connection.setDownloadFile(HSFileUtils.createNewFile(downloadItem.getDownloadItemTempFilePath())); //下载到临时文件
            HSHttpConnection.OnDataReceivedListener onDataReceivedListener = new HSHttpConnection.OnDataReceivedListener() {
                @Override
                public void onDataReceived(HSHttpConnection hsHttpConnection, byte[] bytes, long received, long totalSize) {
                    if (totalSize > 0) {
                        if (received <= totalSize) { //下载中
                            final float percent = received * 100 / totalSize;
                            for (OnDownloadUpdateListener onDownloadUpdateListener : onDownloadUpdateListeners) {
                                if (onDownloadUpdateListener != null) {
                                    onDownloadUpdateListener.onDownloadProgressUpdate(downloadItem, percent);
                                }
                            }
                            for (OnDownloadUpdateListener onDownloadUpdateListener : downloadUpdateListeners) {
                                if (onDownloadUpdateListener != null) {
                                    onDownloadUpdateListener.onDownloadProgressUpdate(downloadItem, percent);
                                }
                            }
                        }
                    }
                }
            };
            HSHttpConnection.OnConnectionFinishedListener onConnectionFinishedListener = new HSHttpConnection.OnConnectionFinishedListener() {
                @Override
                public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                    downloadingItems.remove(downloadItem);
                    if (hsHttpConnection.isSucceeded()) {
                        boolean renameResult = renameFile(new File(downloadItem.getDownloadItemTempFilePath()), new File(downloadItem.getDownloadItemFilePath()));
                        if (renameResult) { // 重命名成功
                            long endTime = SystemClock.elapsedRealtime();
                            long downloadTime = endTime - startTime;
                            for (OnDownloadUpdateListener onDownloadUpdateListener : onDownloadUpdateListeners) {
                                if (onDownloadUpdateListener != null) {
                                    onDownloadUpdateListener.onDownloadSuccess(downloadItem, downloadTime);
                                }
                            }
                            for (OnDownloadUpdateListener onDownloadUpdateListener : downloadUpdateListeners) {
                                if (onDownloadUpdateListener != null) {
                                    onDownloadUpdateListener.onDownloadSuccess(downloadItem, downloadTime);
                                }
                            }
                            KCAnalytics.logEvent(downloadItem.getEventType() + "_download_succeed", "name", downloadItem.getName(), "downloadTime", String.valueOf(downloadTime));
                            if (downloadItem.isNeedUncompress()) { // 需要解压
                                UnzipDownloadItemTask unzipDownloadItemTask = new UnzipDownloadItemTask(onDownloadUpdateListeners, downloadItem);
                                unzipDownloadItemTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        } else { //重命名失败
                            KCAnalytics.logEvent(downloadItem.getEventType() + "_download_failed", "name", downloadItem.getName());
                            for (OnDownloadUpdateListener onDownloadUpdateListener : onDownloadUpdateListeners) {
                                if (onDownloadUpdateListener != null) {
                                    onDownloadUpdateListener.onDownloadFailure(downloadItem);
                                }
                            }
                            for (OnDownloadUpdateListener onDownloadUpdateListener : downloadUpdateListeners) {
                                if (onDownloadUpdateListener != null) {
                                    onDownloadUpdateListener.onDownloadFailure(downloadItem);
                                }
                            }
                            HSFileUtils.delete(new File(downloadItem.getDownloadItemFilePath()));
                        }
                    } else {
                        KCAnalytics.logEvent(downloadItem.getEventType() + "_download_failed", "name", downloadItem.getName());
                        for (OnDownloadUpdateListener onDownloadUpdateListener : onDownloadUpdateListeners) {
                            if (onDownloadUpdateListener != null) {
                                onDownloadUpdateListener.onDownloadFailure(downloadItem);
                            }
                        }
                        for (OnDownloadUpdateListener onDownloadUpdateListener : downloadUpdateListeners) {
                            if (onDownloadUpdateListener != null) {
                                onDownloadUpdateListener.onDownloadFailure(downloadItem);
                            }
                        }
                        HSFileUtils.delete(new File(downloadItem.getDownloadItemFilePath()));
                    }
                }

                @Override
                public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                    downloadingItems.remove(downloadItem);
                    KCAnalytics.logEvent(downloadItem.getEventType() + "_download_failed", "name", downloadItem.getName());
                    for (OnDownloadUpdateListener onDownloadUpdateListener : onDownloadUpdateListeners) {
                        if (onDownloadUpdateListener != null) {
                            onDownloadUpdateListener.onDownloadFailure(downloadItem);
                        }
                    }
                    for (OnDownloadUpdateListener onDownloadUpdateListener : downloadUpdateListeners) {
                        if (onDownloadUpdateListener != null) {
                            onDownloadUpdateListener.onDownloadFailure(downloadItem);
                        }
                    }
                }
            };
            connection.setConnectionFinishedListener(onConnectionFinishedListener);
            connection.setDataReceivedListener(onDataReceivedListener);
            downloadingItems.put(downloadItem, connection);
            for (OnDownloadUpdateListener onDownloadUpdateListener : onDownloadUpdateListeners) {
                if (onDownloadUpdateListener != null) {
                    onDownloadUpdateListener.onDownloadStart(downloadItem);
                }
            }
            for (OnDownloadUpdateListener onDownloadUpdateListener : downloadUpdateListeners) {
                if (onDownloadUpdateListener != null) {
                    onDownloadUpdateListener.onDownloadStart(downloadItem);
                }
            }
            KCAnalytics.logEvent(downloadItem.getEventType() + "_download_begin", "name", downloadItem.getName());
            HSAnalytics.logEventToAppsFlyer(downloadItem.getEventType() + "_download_begin");
            connection.startAsync();
            return true;
        } else {
            return false;
        }
    }

    public void cancel(@NonNull DownloadItem downloadItem) {
        HSHttpConnection hsHttpConnection = downloadingItems.remove(downloadItem);
        if (hsHttpConnection != null) {
            hsHttpConnection.cancel();
        }
        UnzipDownloadItemTask unzipDownloadItemTask = uncompressingItems.get(downloadItem);
        if (unzipDownloadItemTask != null) {
            unzipDownloadItemTask.cancel(true);
        }
    }

    public boolean isDownloadingItem(DownloadItem downloadItem) {
        return downloadingItems.containsKey(downloadItem);
    }

    public boolean isUncompressingItem(DownloadItem downloadItem) {
        return uncompressingItems.containsKey(downloadItem);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addOnDownloadUpdateListener(DownloadItem downloadItem, OnDownloadUpdateListener listener) {
        if (!downloadItemListenerMap.containsKey(downloadItem)) {
            List<OnDownloadUpdateListener> onDownloadUpdateListeners = new ArrayList<>();
            onDownloadUpdateListeners.add(listener);
            downloadItemListenerMap.put(downloadItem, onDownloadUpdateListeners);
            return true;
        } else {
            List<OnDownloadUpdateListener> listeners = downloadItemListenerMap.get(downloadItem);
            if (listeners == null) {
                Toast.makeText(HSApplication.getContext(), "add listener failed", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return listeners.add(listener);
            }
        }
    }

    public boolean removeOnDownloadUpdateListener(DownloadItem downloadItem, OnDownloadUpdateListener listener) {
        if (!downloadItemListenerMap.containsKey(downloadItem)) {
            return false;
        } else {
            List<OnDownloadUpdateListener> listeners = downloadItemListenerMap.get(downloadItem);
            if (listeners == null) {
                return false;
            } else {
                return listeners.remove(listener);
            }
        }
    }

    public boolean addCommonDownloadUpdateListener(OnDownloadUpdateListener listener) {
        return downloadUpdateListeners.add(listener);
    }

    public boolean removeCommonDownloadUpdateListener(OnDownloadUpdateListener listener) {
        return downloadUpdateListeners.remove(listener);
    }

    private class UnzipDownloadItemTask extends AsyncTask<Void, Void, Boolean> {
        List<OnDownloadUpdateListener> onDownloadUpdateListeners;
        private DownloadItem downloadItem;

        /**
         * Creates a new asynchronous task. This constructor must be invoked on the UI thread.
         */
        UnzipDownloadItemTask(List<OnDownloadUpdateListener> onDownloadUpdateListeners, @NonNull DownloadItem downloadItem) {
            this.onDownloadUpdateListeners = onDownloadUpdateListeners;
            this.downloadItem = downloadItem;
        }

        @Override
        protected void onPreExecute() {
            uncompressingItems.put(downloadItem, this);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return unzipItemToTempDir(downloadItem);
        }

        @Override
        protected void onPostExecute(Boolean unzipResult) {
            uncompressingItems.remove(downloadItem);
            if (unzipResult) { //解压成功
                boolean renameResult = renameFile(new File(downloadItem.getDownloadItemFileStoreTempPath()), new File(downloadItem.getDownloadItemFileStorePath()));
                if (renameResult) { //重命名成功
                    for (OnDownloadUpdateListener onDownloadUpdateListener : onDownloadUpdateListeners) {
                        if (onDownloadUpdateListener != null) {
                            onDownloadUpdateListener.onUncompressSuccess(downloadItem);
                        }
                    }
                    for (OnDownloadUpdateListener onDownloadUpdateListener : downloadUpdateListeners) {
                        if (onDownloadUpdateListener != null) {
                            onDownloadUpdateListener.onUncompressSuccess(downloadItem);
                        }
                    }
                } else { //重命名失败
                    for (OnDownloadUpdateListener onDownloadUpdateListener : onDownloadUpdateListeners) {
                        if (onDownloadUpdateListener != null) {
                            onDownloadUpdateListener.onUncompressFailure(downloadItem);
                        }
                    }
                    for (OnDownloadUpdateListener onDownloadUpdateListener : downloadUpdateListeners) {
                        if (onDownloadUpdateListener != null) {
                            onDownloadUpdateListener.onUncompressFailure(downloadItem);
                        }
                    }
                    HSFileUtils.delete(new File(downloadItem.getDownloadItemFileStorePath()));
                }
            } else { //解压失败
                for (OnDownloadUpdateListener onDownloadUpdateListener : onDownloadUpdateListeners) {
                    if (onDownloadUpdateListener != null) {
                        onDownloadUpdateListener.onUncompressFailure(downloadItem);
                    }
                }
                for (OnDownloadUpdateListener onDownloadUpdateListener : downloadUpdateListeners) {
                    if (onDownloadUpdateListener != null) {
                        onDownloadUpdateListener.onUncompressFailure(downloadItem);
                    }
                }
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            uncompressingItems.remove(downloadItem);
        }
    }

    private boolean renameFile(File srcFile, File dstFile) {
        return srcFile.renameTo(dstFile);
    }

    private boolean unzipItemToTempDir(@NonNull DownloadItem downloadItem) {
        try {
            HSZipUtils.unzip(new File(downloadItem.getDownloadItemFilePath()), new File(downloadItem.getDownloadItemFileStoreTempPath()));
            return true;
        } catch (ZipException e) {
            e.printStackTrace();
            HSFileUtils.delete(new File(downloadItem.getDownloadItemFileStoreTempPath()));
            return false;
        } finally {
            HSFileUtils.delete(new File(downloadItem.getDownloadItemFilePath()));
        }
    }
}

package com.ihs.inputmethod.uimodules.mediacontroller.downloads;

import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSZipUtils;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.DownloadStatusListener;

import java.io.File;

public final class ZipDownload extends BaseDownload {

    private DownloadStatusListener callback;
    private File downloadFile;
    private File destDir;

    public ZipDownload(String url, File downloadedFile, File destDir, final DownloadStatusListener callback) {
        super(url);
        this.downloadFile = downloadedFile;
        this.destDir = destDir;
        this.filePath = destDir.getAbsolutePath();
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            HSLog.e(downloadFile.getAbsolutePath());
            download(downloadFile);
            HSZipUtils.unzip(downloadFile, destDir);

            if (callback != null) {
                HSLog.e("1");
                callback.onDownloadSucceeded(downloadFile);
            }
        } catch (Exception e) {
            HSLog.e("2");
            if (callback != null) {
                callback.onDownloadFailed(downloadFile);
            }

            e.printStackTrace();
        } finally {
            downloadFile.delete();
            MediaController.getDownloadManger().getDownloadQueue().removeRunningDownload(this);
        }
    }

    @Override
    protected void download(File downloadedFile) throws Exception {
        HSLog.d(downloadedFile.getAbsolutePath());
        HSHttpConnection con = new HSHttpConnection(url, HttpRequest.Method.GET);
        con.setConnectTimeout(1000 * 6);
        con.setDownloadFile(downloadedFile);
        con.setDataReceivedListener(new HSHttpConnection.OnDataReceivedListener() {
            @Override
            public void onDataReceived(HSHttpConnection hsHttpConnection, byte[] bytes, long l, long l1) {
                if (l1 > 0) {
                    final float percent = (float) l / l1;
                    if (callback != null) {
                        callback.onDownloadProgress(downloadFile, percent);
                    }
                }
            }
        });

        con.startSync();
        if (!con.isSucceeded()) {
            throw new Exception("download error");
        }
    }
}

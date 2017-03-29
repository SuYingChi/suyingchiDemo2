package com.ihs.inputmethod.uimodules.mediacontroller.downloads;

import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.DownloadStatusListener;

import java.io.File;
import java.lang.ref.SoftReference;

/**
 * Created by dsapphire on 15/12/15.
 * <p/>
 * manager the download Callback
 */
public final class MediaDownload extends BaseDownload {

    private SoftReference<DownloadStatusListener> callback;
    private File downloadFile;

    /**
     * @param url
     * @param downloadedFile
     * @param callback
     */
    public MediaDownload(String url, File downloadedFile, final DownloadStatusListener callback) {
        super(url);
        this.downloadFile = MediaController.getFileManager().getDownloadGif(downloadedFile.getAbsolutePath());
        this.filePath=this.downloadFile.getAbsolutePath();
        this.callback = new SoftReference<>(callback) ;
        onDataReceivedListener = new OnDataReceivedListener() {
            @Override
            public void onDataReceived(long received, long size) {
                final float percent = (float) received / size;
                callback.onDownloadProgress(new File(filePath), percent);
            }
        };
    }

    public MediaDownload(String url, String downloadedFilePath, final DownloadStatusListener callback) {
        super(url);
        this.downloadFile = new File(downloadedFilePath);
        this.filePath = this.downloadFile.getAbsolutePath();
        this.callback = new SoftReference<>(callback) ;
        onDataReceivedListener = new OnDataReceivedListener() {
            @Override
            public void onDataReceived(long received, long size) {
                final float percent = (float) received / size;
                callback.onDownloadProgress(downloadFile, percent);
            }
        };
    }



    @Override
    public void run() {
        try {
            download(downloadFile);
            if (callback.get() == null) {
                return;
            }
            callback.get().onDownloadSucceeded(downloadFile);
        } catch (Exception e) {
            if (callback.get() == null) {
                return;
            }
            callback.get().onDownloadFailed(downloadFile);
            e.printStackTrace();
        } finally {
            MediaController.getDownloadManger().getDownloadQueue().removeRunningDownload(this);
        }
    }

    @Override
    public void stop() {
        super.stop();
    }


}

package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.flurry.sdk.ao;

import com.ihs.app.analytics.HSAnalytics;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.api.utils.HSZipUtils;
import com.ihs.inputmethod.feature.common.ConcurrentUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipException;

/**
 * Created by yanxia on 2017/7/23.
 */

public class StickerDownloadManager {
    private static final String STICKER_DOWNLOAD_ZIP_SUFFIX = ".zip";

    private static StickerDownloadManager instance;

    private StickerDownloadManager() {
    }

    public static StickerDownloadManager getInstance() {
        if (instance == null) {
            synchronized (StickerDownloadManager.class) {
                if (instance == null) {
                    instance = new StickerDownloadManager();
                }
            }
        }
        return instance;
    }

    private void unzipStickerGroup(String stickerGroupZipFilePath, StickerGroup stickerGroup) {
        try {
            // 下载成功 先解压好下载的zip
            HSZipUtils.unzip(new File(stickerGroupZipFilePath), new File(StickerUtils.getStickerRootFolderPath()));
            StickerDataManager.getInstance().updateStickerGroupList(stickerGroup);
        } catch (ZipException e) {
            Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.unzip_sticker_group_failed), Toast.LENGTH_SHORT).show();
            HSLog.e(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getStickerGroupDownloadFilePath(String stickerGroupName) {
        return StickerUtils.getStickerRootFolderPath() + "/" + stickerGroupName + STICKER_DOWNLOAD_ZIP_SUFFIX;
    }

    public void startForegroundDownloading(Context context, final StickerGroup stickerGroup,
                                           final Drawable thumbnailDrawable, final AdLoadingView.OnAdBufferingListener onAdBufferingListener) {

        final String stickerGroupZipFilePath = getStickerGroupDownloadFilePath(stickerGroup.getStickerGroupName());
        final AdLoadingView adLoadingView = new AdLoadingView(context);
        final Resources resources = HSApplication.getContext().getResources();
        adLoadingView.configParams(null, thumbnailDrawable != null ? thumbnailDrawable : resources.getDrawable(R.drawable.ic_sticker_loading_image),
                resources.getString(R.string.sticker_downloading_label),
                resources.getString(R.string.sticker_downloading_successful),
                resources.getString(R.string.ad_placement_lucky),
                new AdLoadingView.OnAdBufferingListener() {
                    @Override
                    public void onDismiss(boolean downloadSuccess) {
                        if (downloadSuccess) {
                            if (stickerGroup.isStickerGroupDownloaded()) {
                                HSLog.d("sticker " + stickerGroup.getStickerGroupName() + " download succeed");
                            } else {
                                HSLog.e("sticker " + stickerGroup.getStickerGroupName() + " download error!");
                            }
                        } else {
                            // 没下载成功
                            HSHttpConnection connection = (HSHttpConnection) adLoadingView.getTag();
                            if (connection != null) {
                                connection.cancel();
                                HSFileUtils.delete(new File(stickerGroupZipFilePath));
                            }
                        }
                        if (onAdBufferingListener != null) {
                            onAdBufferingListener.onDismiss(downloadSuccess);
                        }
                    }
                }, 2000, false);
        adLoadingView.showInDialog();

        HSHttpConnection connection = new HSHttpConnection(stickerGroup.getStickerGroupDownloadUri());
        connection.setDownloadFile(HSFileUtils.createNewFile(stickerGroupZipFilePath));
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.e("startForegroundDownloading onConnectionFailed hsError" + hsError.getMessage());
                adLoadingView.setConnectionStateText(resources.getString(R.string.foreground_download_failed));
                adLoadingView.setConnectionProgressVisibility(View.INVISIBLE);
            }
        });
        connection.setDataReceivedListener(new HSHttpConnection.OnDataReceivedListener() {
            @Override
            public void onDataReceived(HSHttpConnection hsHttpConnection, byte[] bytes, long received, long totalSize) {
                if (totalSize > 0) {
                    final float percent = (float) received * 100 / totalSize;
                    if (received >= totalSize) {
                        HSAnalytics.logEvent("sticker_download_succeed", "stickerGroupName", stickerGroup.getStickerGroupName());
                        unzipStickerGroup(stickerGroupZipFilePath, stickerGroup);
                    }
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            adLoadingView.updateProgressPercent((int) percent);
                        }
                    });
                }
            }
        });
        connection.startAsync();
        adLoadingView.setTag(connection);
    }

    /**
     *
     * Try to copy a asset sticker file to sd card.
     * @param sticker
     * @param v
     * @param callback
     */
    public  void tryLoadAssetSticker(Sticker sticker, View v,LoadingAssetStickerCallback callback) {
        AssetStickerProcessTask task = new AssetStickerProcessTask(sticker,v,callback);
        ConcurrentUtils.postOnThreadPoolExecutor (task);
    }

    /**
     * Copy asset file to SD card .
     */
    private static class AssetStickerProcessTask implements Runnable {
        SoftReference<View> view;
        public Sticker sticker ;
        LoadingAssetStickerCallback callback;
        public File resultFile;

        public AssetStickerProcessTask(Sticker sticker,View v,LoadingAssetStickerCallback callback) {
            this.sticker = sticker;
            this.view = new SoftReference<View>(v);
            this.resultFile = new File(StickerUtils.getStickerLocalPath(this.sticker));
            this.callback =  callback;
        }

        @Override
        public void run() {
            if (this.sticker == null) {
                UIController.getInstance().getUIHandler().post(new Runnable() {
                    @Override
                    public void run() {
                         callback.processFailed(sticker,new Exception("Null sticker"));
                    }
                });
                return  ;
            }
            if (this.resultFile.exists()) {
                UIController.getInstance().getUIHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        callback.processSucceeded(sticker, resultFile, view.get() );
                    }
                });
                return   ;
            }

            String stickerFileName = sticker.getStickerName() + sticker.getStickerFileSuffix();
            String stickerAssetFolderPath = StickerUtils.getStickerAssetFolderPath(sticker);
            AssetManager assetManager = HSApplication.getContext(). getAssets();
            String[] files = null;
            try {
                files = assetManager.list(stickerAssetFolderPath);
            } catch (IOException e) {
                Log.e("tag", "Failed to get asset file list.", e);
            }

            if (files != null) {
                for (String filename : files) {
                    if (!filename.endsWith(stickerFileName)) {
                        continue;
                    }
                    InputStream in = null;
                    OutputStream out = null;
                    try {

                        File folder = this.resultFile.getParentFile();
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                        if (!this.resultFile.exists()) {
                            this.resultFile.createNewFile();
                        }

                        in = assetManager.open(stickerAssetFolderPath + File.separator + filename);
                        out = new FileOutputStream(this.resultFile);
                        copyFile(in, out);

                        UIController.getInstance().getUIHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                callback.processSucceeded(sticker, resultFile,view.get() );
                            }
                        });

                    } catch (final IOException e) {
                        Log.e("tag", "Failed to copy asset file: " + filename, e);
                        UIController.getInstance().getUIHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                callback.processFailed(sticker,e);
                            }
                        });
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                // NOOP
                            }
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                // NOOP
                            }
                        }
                    }

                    break;
                }
            }else {

                UIController.getInstance().getUIHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        callback.processFailed(sticker,new Exception("can't find the asset sticker file"));
                    }
                });
            }
        }

        private void copyFile(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
        }

    }

    public interface LoadingAssetStickerCallback{
        void processSucceeded(Sticker sticker,File file, View view);
        void processFailed(Sticker sticker, Exception e);
    }

}

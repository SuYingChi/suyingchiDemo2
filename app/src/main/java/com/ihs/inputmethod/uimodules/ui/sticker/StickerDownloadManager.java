package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.utils.HSZipUtils;
import com.ihs.inputmethod.emoji.StickerSuggestionManager;
import com.ihs.inputmethod.feature.common.ConcurrentUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;
import com.ihs.inputmethod.utils.DownloadUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

/**
 * Created by yanxia on 2017/7/23.
 */

public class StickerDownloadManager {
    // --Commented out by Inspection (18/1/11 下午2:41):private static final String STICKER_DOWNLOAD_ZIP_SUFFIX = ".zip";
    // --Commented out by Inspection (18/1/11 下午2:41):private static final String STICKER_DOWNLOAD_JSON_SUFFIX = ".json";
    private static final String DOWNLOADED_STICKER_NAME_JOIN = "download_sticker_name_join";


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

    public List<String> getDownloadedStickerFileList() {
        List<String> stickerNames = new ArrayList<>();
        String stickerDownloadNames = HSPreferenceHelper.getDefault().getString(DOWNLOADED_STICKER_NAME_JOIN, "");
        try {
            JSONArray jsonArray = new JSONArray(stickerDownloadNames);
            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                stickerNames.add((String) jsonArray.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stickerNames;
    }

    public void unzipStickerGroup(String stickerGroupZipFilePath, StickerGroup stickerGroup) {
        try {
            // 下载成功 先解压好下载的zip
            HSZipUtils.unzip(new File(stickerGroupZipFilePath), new File(StickerUtils.getStickerRootFolderPath()));
            DownloadUtils.getInstance().saveJsonArrayToPref(DOWNLOADED_STICKER_NAME_JOIN, stickerGroup.getStickerGroupName());
            StickerDataManager.getInstance().updateStickerGroupList(stickerGroup);
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    StickerSuggestionManager.getInstance().updateConfig(false);
                }
            });
        } catch (ZipException e) {
            Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.unzip_sticker_group_failed), Toast.LENGTH_SHORT).show();
            HSLog.e(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Try to copy a asset sticker file to sd card.
     *
     * @param sticker
     * @param v
     * @param callback
     */
    public void tryLoadAssetSticker(Sticker sticker, View v, LoadingAssetStickerCallback callback) {
        AssetStickerProcessTask task = new AssetStickerProcessTask(sticker, v, callback);
        ConcurrentUtils.postOnThreadPoolExecutor(task);
    }

    /**
     * Copy asset file to SD card .
     */
    private static class AssetStickerProcessTask implements Runnable {
        SoftReference<View> view;
        public Sticker sticker;
        LoadingAssetStickerCallback callback;
        public File resultFile;

        public AssetStickerProcessTask(Sticker sticker, View v, LoadingAssetStickerCallback callback) {
            this.sticker = sticker;
            this.view = new SoftReference<View>(v);
            this.resultFile = new File(StickerUtils.getStickerLocalPath(this.sticker));
            this.callback = callback;
        }

        @Override
        public void run() {
            if (this.sticker == null) {
                UIController.getInstance().getUIHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        callback.processFailed(sticker, new Exception("Null sticker"));
                    }
                });
                return;
            }
            if (this.resultFile.exists()) {
                UIController.getInstance().getUIHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        callback.processSucceeded(sticker, resultFile, view.get());
                    }
                });
                return;
            }

            String stickerFileName = sticker.getStickerName() + sticker.getStickerFileSuffix();
            String stickerAssetFolderPath = StickerUtils.getStickerAssetFolderPath(sticker);
            AssetManager assetManager = HSApplication.getContext().getAssets();
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
                                callback.processSucceeded(sticker, resultFile, view.get());
                            }
                        });

                    } catch (final IOException e) {
                        Log.e("tag", "Failed to copy asset file: " + filename, e);
                        UIController.getInstance().getUIHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                callback.processFailed(sticker, e);
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
            } else {

                UIController.getInstance().getUIHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        callback.processFailed(sticker, new Exception("can't find the asset sticker file"));
                    }
                });
            }
        }

        private void copyFile(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

    }

    public interface LoadingAssetStickerCallback {
        void processSucceeded(Sticker sticker, File file, View view);

        void processFailed(Sticker sticker, Exception e);
    }

}

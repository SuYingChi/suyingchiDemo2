package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.api.utils.HSZipUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.kc.commons.configfile.KCList;
import com.kc.commons.configfile.KCParser;

import org.json.JSONArray;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

/**
 * Created by yanxia on 2017/7/23.
 */

public class StickerDownloadManager {
    private static final String STICKER_DOWNLOAD_ZIP_SUFFIX = ".zip";
    private static final String STICKER_DOWNLOAD_JSON_SUFFIX = ".json";
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
        File file = new File(getDownloadedStickerNameList());
        KCList kcList = KCParser.parseList(file);
        if (kcList == null) {
            return null;
        }
        for (int i = 0; i < kcList.size(); i++) {
            String downloadStickerName = kcList.getString(i);
            stickerNames.add(downloadStickerName);
        }
        return stickerNames;
    }

    public void unzipStickerGroup(String stickerGroupZipFilePath, StickerGroup stickerGroup) {
        try {
            // 下载成功 先解压好下载的zip
            HSZipUtils.unzip(new File(stickerGroupZipFilePath), new File(StickerUtils.getStickerRootFolderPath()));
            writeJsonToFile(updateJsonArray(stickerGroup.getStickerGroupName()), getDownloadedStickerNameList());
            StickerDataManager.getInstance().updateStickerGroupList(stickerGroup);
        } catch (ZipException e) {
            Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.unzip_sticker_group_failed), Toast.LENGTH_SHORT).show();
            HSLog.e(e.getMessage());
            e.printStackTrace();
        }
    }

    private JSONArray updateJsonArray(String fileName) {
        JSONArray jsonArray = new JSONArray();
        File file = new File(getDownloadedStickerNameList());
        KCList kcList = KCParser.parseList(file);
        if (kcList == null) {
            jsonArray.put(fileName);
        } else {
            for (int i = 0; i < kcList.size(); i++) {
                jsonArray.put(kcList.getString(i));
            }
            jsonArray.put(fileName);
        }
        return jsonArray;
    }

    private void writeJsonToFile(JSONArray jsonArray, String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            DataOutputStream out = new DataOutputStream(new FileOutputStream(
                    file));
            out.writeBytes(jsonArray.toString());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getStickerGroupDownloadFilePath(String stickerGroupName) {
        return StickerUtils.getStickerRootFolderPath() + "/" + stickerGroupName + STICKER_DOWNLOAD_ZIP_SUFFIX;
    }

    private String getDownloadedStickerNameList() {
        return StickerUtils.getStickerRootFolderPath() + "/" + DOWNLOADED_STICKER_NAME_JOIN + STICKER_DOWNLOAD_JSON_SUFFIX;
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
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent("sticker_download_succeed", stickerGroup.getStickerGroupName());
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
}

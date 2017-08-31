package com.ihs.inputmethod.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontDownloadManager;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.FontModel;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDownloadManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.kc.commons.configfile.KCList;
import com.kc.commons.configfile.KCParser;

import org.json.JSONArray;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontDownloadManager.ASSETS_FONT_FILE_PATH;
import static com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontDownloadManager.JSON_SUFFIX;
import static com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils.STICKER_DOWNLOAD_ZIP_SUFFIX;

/**
 * Created by guonan.lv on 17/8/31.
 */

public class DownloadUtils {
    private static DownloadUtils instance;
    private String filePath;
    private String objectName;

    public static DownloadUtils getInstance() {
        if (instance == null) {
            synchronized (DownloadUtils.class) {
                if (instance == null) {
                    instance = new DownloadUtils();
                }
            }
        }
        return instance;
    }

    public JSONArray updateJsonArray(String fileName, String filePath) {
        JSONArray jsonArray = new JSONArray();
        File file = new File(filePath);
        KCList kcList = KCParser.parseList(file);
        if (kcList == null) {
            jsonArray.put(fileName);
        } else {
            jsonArray.put(fileName);
            for (int i = 0; i < kcList.size(); i++) {
                jsonArray.put(kcList.getString(i));
            }
        }
        return jsonArray;
    }

    public void writeJsonToFile(String fileName, String filePath) {
        JSONArray jsonArray = updateJsonArray(fileName, filePath);
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

    public String getFontDownloadFilePath(String fontName) {
        return HSApplication.getContext().getFilesDir() + File.separator + ASSETS_FONT_FILE_PATH + "/" + fontName + JSON_SUFFIX;
    }

    private String getStickerGroupDownloadFilePath(String stickerGroupName) {
        return StickerUtils.getStickerRootFolderPath() + "/" + stickerGroupName + STICKER_DOWNLOAD_ZIP_SUFFIX;
    }

    private void initConnection(final Resources resources, final Object object, final AdLoadingView adLoadingView, final HSHttpConnection connection) {
        connection.setDownloadFile(HSFileUtils.createNewFile(filePath));
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
                        if (object instanceof FontModel) {
                            HSFontDownloadManager.getInstance().updateFontModel((FontModel)object);
                            HSGoogleAnalyticsUtils.getInstance().logAppEvent("font_download_succeed", objectName);
                        } else if (object instanceof StickerGroup) {
                            HSGoogleAnalyticsUtils.getInstance().logAppEvent("sticker_download_succeed", objectName);
                            StickerDownloadManager.getInstance().unzipStickerGroup(filePath, (StickerGroup)object);
                        }
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
    }

    public void startForegroundDownloading(Context context, final Object object,
                                           final Drawable thumbnailDrawable, final AdLoadingView.OnAdBufferingListener onAdBufferingListener) {
        HSHttpConnection connection;
        if (object instanceof FontModel) {
            objectName = ((FontModel)object).getFontName();
            filePath = getFontDownloadFilePath(((FontModel)object).getFontName());
            connection = new HSHttpConnection(((FontModel)object).getFontDownloadBaseURL());
        } else if (object instanceof StickerGroup) {
            objectName = ((StickerGroup)object).getStickerGroupName();
            filePath = getStickerGroupDownloadFilePath(((StickerGroup)object).getStickerGroupName());
            connection = new HSHttpConnection(((StickerGroup)object).getStickerGroupDownloadUri());
        } else {
            return;
        }

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
                            HSLog.e("eee", "downloadSuccess");
                        } else {
                            // 没下载成功
                            HSHttpConnection connection = (HSHttpConnection) adLoadingView.getTag();
                            if (connection != null) {
                                connection.cancel();
                                HSFileUtils.delete(new File(filePath));
                            }
                        }
                        if (onAdBufferingListener != null) {
                            onAdBufferingListener.onDismiss(downloadSuccess);
                        }
                    }
                }, 2000, false);
        adLoadingView.showInDialog();

        initConnection(resources, object, adLoadingView, connection);
        adLoadingView.setTag(connection);
    }
}

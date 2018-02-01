package com.ihs.inputmethod.uimodules.ui.theme.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.api.utils.HSZipUtils;
import com.ihs.inputmethod.theme.KeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.DisplayUtils;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipException;

public class ThemeZipDownloadUtils {

    /**
     *
     * @param themeName 主题名
     * @param thumbnailUrl 预览图地址
     * @param from 调用来源，用于统计事件
     * @param onAdBufferingListener
     */
    public static void startDownloadThemeZip(Context context,String from, final String themeName, final String thumbnailUrl, final AdLoadingView.OnAdBufferingListener onAdBufferingListener) {

        File themeDirectory = new File(KeyboardThemeManager.getThemeDirectoryPath(themeName));
        File downloadFile = new File(themeDirectory, System.currentTimeMillis() + ".zip");
        if (!themeDirectory.exists()) {
            themeDirectory.mkdirs();
        }

        final AdLoadingView adLoadingView = new AdLoadingView(context);
        final Resources resources = context.getResources();
        adLoadingView.configParams(null,null,
                resources.getString(R.string.sticker_downloading_label),
                resources.getString(R.string.sticker_downloading_successful),
                resources.getString(R.string.ad_placement_applying),
                (downloadSuccess, manually) -> {
                    if (downloadSuccess) {
                        //设置下载成功移到此处，如果用户在AdLoadingView最后的缓冲时间内点击close按钮则应该不设置为下载成功
                        HSKeyboardThemeManager.setThemeZipFileDownloadAndUnzipSuccess(themeName);
                    } else {
                        // 没下载成功
                        HSHttpConnection conn = (HSHttpConnection) adLoadingView.getTag();
                        if (conn != null) {
                            conn.cancel();
                        }
                    }
                    HSFileUtils.delete(downloadFile);
                    if (onAdBufferingListener != null) {
                        onAdBufferingListener.onDismiss(downloadSuccess, manually);
                    }
                }, 2000, false);

        ImageView thumbnailImageView = adLoadingView.findViewById(R.id.iv_icon);
        ImageSize imageSize = new ImageSize(DisplayUtils.dip2px(HSApplication.getContext(),100),DisplayUtils.dip2px(HSApplication.getContext(),100));
        ImageLoader.getInstance().displayImage(thumbnailUrl, new ImageViewAware(thumbnailImageView),  new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY).build(), imageSize, null, null);
        adLoadingView.showInDialog();

        HSHttpConnection connection = new HSHttpConnection(HSConfig.getString("Application","Server","ThemeZipDownloadBaseURL") + themeName + ".zip");
        connection.setDownloadFile(downloadFile);
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                logDownloadFailedEvent(themeName,from);
                adLoadingView.setConnectionStateText(resources.getString(R.string.foreground_download_failed));
            }
        });
        int initialProgress = 1;
        connection.setHeaderReceivedListener(hsHttpConnection -> new Handler().post(() -> adLoadingView.updateProgressPercent(initialProgress)));
        connection.setDataReceivedListener((hsHttpConnection, bytes, received, totalSize) -> {
            if (totalSize > 0) {
                if (received >= totalSize) {
                    File desFile = new File(KeyboardThemeManager.getThemeRootDirectoryPath());
                    try {
                        HSZipUtils.unzip(downloadFile,desFile);
                    } catch (ZipException e) {
                        e.printStackTrace();
                        logDownloadFailedEvent(themeName,from);
                        adLoadingView.setConnectionStateText(resources.getString(R.string.foreground_download_failed));
                        adLoadingView.updateProgressPercent(0);
                        adLoadingView.setConnectionProgressVisibility(View.INVISIBLE);
                        return;
                    }
                }
                final long percent = received * 100 / totalSize;
                new Handler().post(() -> adLoadingView.updateProgressPercent((int) percent));
            }
        });
        connection.startAsync();
        adLoadingView.setTag(connection);
    }

    public static void logDownloadClickEvent(String themeName, String from) {
        HSAnalytics.logEvent("theme_downloadInApp_clicked",getDownloadParamMap(themeName, from));
    }
    public static void logDownloadSuccessEvent(String themeName, String from) {
        HSAnalytics.logEvent("theme_downloadInApp_success",getDownloadParamMap(themeName, from));
    }

    private static void logDownloadFailedEvent(String themeName, String from) {
        HSAnalytics.logEvent("theme_downloadInApp_failed",getDownloadParamMap(themeName, from));
    }

    @NonNull
    private static Map<String, String> getDownloadParamMap(String themeName, String from) {
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("from",from);
        paramMap.put("themeName",themeName);
        return paramMap;
    }
}

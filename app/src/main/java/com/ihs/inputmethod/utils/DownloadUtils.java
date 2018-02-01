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
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.kc.utils.KCAnalytics;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

/**
 * Created by guonan.lv on 17/8/31.
 */

public class DownloadUtils {
    private static DownloadUtils instance;
    private String filePath;
    // --Commented out by Inspection (18/1/11 下午2:41):private String objectName;

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

    public boolean saveJsonArrayToPref(String key, String value) {
        try {
            String originValue = HSPreferenceHelper.getDefault().getString(key, "");
            JSONArray jsonArray;
            if ("".equals(originValue)) {
                jsonArray = new JSONArray();
            } else {
                jsonArray = new JSONArray(originValue);
            }
            jsonArray.put(value);
            HSPreferenceHelper.getDefault().putString(key, jsonArray.toString());
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void initConnection(final Resources resources, final AdLoadingView adLoadingView, final HSHttpConnection connection) {
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
        int initialProgress = 1;
        connection.setHeaderReceivedListener(hsHttpConnection -> new Handler().post(() -> adLoadingView.updateProgressPercent(initialProgress)));
        connection.setDataReceivedListener((hsHttpConnection, bytes, received, totalSize) -> {
            if (totalSize > 0) {
                final float percent = (float) received * 100 / totalSize;
                new Handler().post(() -> adLoadingView.updateProgressPercent((int) percent));
            }
        });
        connection.startAsync();
    }

    public void startForegroundDownloading(Context context, final String objectName, final String filePath, final String downloadUrl,
                                           final Drawable thumbnailDrawable, final AdLoadingView.OnAdBufferingListener onAdBufferingListener, boolean showInDialog) {
        HSHttpConnection connection;
        this.filePath = filePath;
        connection = new HSHttpConnection(downloadUrl);

        final AdLoadingView adLoadingView = new AdLoadingView(context);
        final Resources resources = HSApplication.getContext().getResources();
        adLoadingView.configParams(null, thumbnailDrawable != null ? thumbnailDrawable : resources.getDrawable(R.drawable.ic_sticker_loading_image),
                resources.getString(R.string.sticker_downloading_label),
                resources.getString(R.string.sticker_downloading_successful),
                resources.getString(R.string.ad_placement_applying),
                (downloadSuccess, manually) -> {
                    if (downloadSuccess) {
                    } else {
                        // 没下载成功
                        HSHttpConnection connection1 = (HSHttpConnection) adLoadingView.getTag();
                        if (connection1 != null) {
                            connection1.cancel();
                            HSFileUtils.delete(new File(filePath));
                        }
                    }
                    if (onAdBufferingListener != null) {
                        onAdBufferingListener.onDismiss(downloadSuccess, manually);
                    }
                }, 2000, false);
        adLoadingView.showInDialog();
        KCAnalytics.logEvent("app_alert_applyingItem_show");

        initConnection(resources, adLoadingView, connection);
        adLoadingView.setTag(connection);
    }

    public void startForegroundDownloading(Context context, final String objectName, final String filePath, final String downloadUrl,
                                           final Drawable thumbnailDrawable, final AdLoadingView.OnAdBufferingListener onAdBufferingListener) {
        startForegroundDownloading(context, objectName, filePath, downloadUrl, thumbnailDrawable, onAdBufferingListener, true);
    }
}

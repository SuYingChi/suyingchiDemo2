package com.ihs.inputmethod.uimodules.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.ImageView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.constants.AdPlacements;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.DownloadItem;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.kc.utils.KCAnalytics;

/**
 * DownloadUtils
 * Created by yanxia on 2017/11/6.
 */

public class DownloadUtils {
    public static final String DOWNLOAD_ITEM_TYPE_STICKER_GROUP = "Stickers";
    @SuppressWarnings("WeakerAccess")
    public static final String DOWNLOAD_ITEM_TYPE_FILTER = "lookupFilter";

    public static final int DOWNLOAD_CODE_ERROR = -1;
    public static final int DOWNLOAD_CODE_SUCCESS = 101;

    private static final String DOWNLOAD_ITEM_FILE_SUFFIX_ZIP = ".zip";
    private static final String DOWNLOAD_ITEM_FILE_SUFFIX_PNG = ".png";

    public static final int DOWNLOAD_STATUS_NEED_DOWNLOAD = 0;
    public static final int DOWNLOAD_STATUS_DOWNLOADING = 1;
    public static final int DOWNLOAD_STATUS_DOWNLOADED = 2;

    private DownloadUtils() { // 不要实例化DownloadUtils
        throw new AssertionError();
    }

    public interface OnDownloadAlertListener {
        void onUnlockActionClicked();

        void onAlertShow();

        void onDismiss(boolean success);
    }

    public static DownloadItem createStickerGroupDownloadItem(@NonNull String stickerGroupName) {
        return new DownloadItem(stickerGroupName, DOWNLOAD_ITEM_TYPE_STICKER_GROUP, StickerGroup.STICKER_REMOTE_ROOT_DIR_NAME, DOWNLOAD_ITEM_FILE_SUFFIX_ZIP, true, "stickerGroup");
    }


    public static boolean startDownloadBackground(@NonNull DownloadItem downloadItem) {
        return CommonDownloadManager.getInstance().downloadItem(downloadItem, null);
    }

    public static void startDownloadForeground(@NonNull Activity context,
                                               @NonNull DownloadItem downloadItem,
                                               @Nullable final Drawable icon,
                                               @Nullable final OnDownloadAlertListener downloadAlertListener,
                                               @Nullable final String from,
                                               boolean showInterstitialAd) {
        downloadItemWithAdLoadingView(context, downloadItem, icon, downloadAlertListener, showInterstitialAd, from);
    }

    private static void downloadItemWithAdLoadingView(Activity context,
                                                      DownloadItem downloadItem,
                                                      final Drawable icon,
                                                      final OnDownloadAlertListener downloadAlertListener,
                                                      final boolean showInterstitialAd,
                                                      String from) {
        KCAnalytics.logEvent("effects_download", "from", TextUtils.isEmpty(from) ? "unknown" : from, "type", downloadItem.getEventType());
        final Resources resources = HSApplication.getContext().getResources();
        final AdLoadingView adLoadingView = new AdLoadingView(context);
        CommonDownloadManager.OnDownloadUpdateListener listener = new CommonDownloadManager.OnDownloadUpdateListener() {

            @Override
            public void onDownloadStart(DownloadItem downloadItem) {

            }

            @Override
            public void onDownloadProgressUpdate(DownloadItem downloadItem, float percent) {
                int fakeProgress; // 剩下的5%留给解压
                if (percent - 5 > 0) {
                    fakeProgress = (int) (percent - 5);
                } else {
                    fakeProgress = 0;
                }
                adLoadingView.updateProgressPercent(fakeProgress);
            }

            @Override
            public void onDownloadSuccess(DownloadItem downloadItem, long downloadTime) {
                if (!downloadItem.isNeedUncompress()) {
                    adLoadingView.updateProgressPercent(100);
                }
            }

            @Override
            public void onDownloadFailure(DownloadItem downloadItem) {
                adLoadingView.setConnectionStateText(resources.getString(R.string.foreground_download_failed));
                adLoadingView.updateProgressPercent(0);
            }

            @Override
            public void onUncompressSuccess(DownloadItem downloadItem) {
                adLoadingView.updateProgressPercent(100);
            }

            @Override
            public void onUncompressFailure(DownloadItem downloadItem) {
                adLoadingView.setConnectionStateText(resources.getString(R.string.unzip_sticker_group_failed));
                adLoadingView.updateProgressPercent(0);
            }
        };
        adLoadingView.configParams(null, icon != null ? icon : ContextCompat.getDrawable(HSApplication.getContext(), R.drawable.ic_theme_gift),
                resources.getString(R.string.downloading),
                resources.getString(R.string.sticker_downloading_label),
                AdPlacements.NATIVE_APPLYING_ITEM,
                new AdLoadingView.OnAdBufferingListener() {
                    @Override
                    public void onDismiss(boolean downloadSuccess, boolean dismissManually) {
                        if (downloadAlertListener != null) {
                            downloadAlertListener.onDismiss(downloadSuccess);
                        }
                        CommonDownloadManager.getInstance().removeOnDownloadUpdateListener(downloadItem, listener);
                    }
                }
                , 4000, false);
        ImageView iconImageView = adLoadingView.findViewById(R.id.iv_icon);
        if (iconImageView != null) {
            iconImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        adLoadingView.showInDialog();
        if (downloadAlertListener != null) {
            downloadAlertListener.onAlertShow();
        }
        CommonDownloadManager.getInstance().downloadItem(downloadItem, listener);
    }

    public static boolean isDownloadingItem(DownloadItem downloadItem) {
        return CommonDownloadManager.getInstance().isDownloadingItem(downloadItem) || CommonDownloadManager.getInstance().isUncompressingItem(downloadItem);
    }
}

package com.ihs.inputmethod.uimodules.utils;

import android.support.annotation.NonNull;

import com.ihs.inputmethod.uimodules.ui.sticker.DownloadItem;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;

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


}

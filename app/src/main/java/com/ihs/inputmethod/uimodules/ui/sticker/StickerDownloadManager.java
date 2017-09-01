package com.ihs.inputmethod.uimodules.ui.sticker;

import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSZipUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.utils.DownloadUtils;
import com.kc.commons.configfile.KCList;
import com.kc.commons.configfile.KCParser;

import java.io.File;
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
            DownloadUtils.getInstance().writeJsonToFile(stickerGroup.getStickerGroupName(), getDownloadedStickerNameList());
//            writeJsonToFile(updateJsonArray(stickerGroup.getStickerGroupName()), getDownloadedStickerNameList());
            StickerDataManager.getInstance().updateStickerGroupList(stickerGroup);
        } catch (ZipException e) {
            Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.unzip_sticker_group_failed), Toast.LENGTH_SHORT).show();
            HSLog.e(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getDownloadedStickerNameList() {
        return StickerUtils.getStickerRootFolderPath() + "/" + DOWNLOADED_STICKER_NAME_JOIN + STICKER_DOWNLOAD_JSON_SUFFIX;
    }
}

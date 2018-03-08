package com.ihs.inputmethod.uimodules.ui.sticker;

import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.utils.HSConfigUtils;

import java.io.File;

/**
 * Created by yanxia on 2017/11/6.
 */

public class DownloadItem {

    private static final String TEMP = "_temp";

    private String name;
    private String type;
    private String remoteType;
    private String fileSuffix;
    private boolean needUncompress;
    private String eventType;

    public DownloadItem(String name, String localType, String remoteType, String suffix, boolean needUncompress, String eventType) {
        this.name = name;
        this.type = localType;
        this.remoteType = remoteType;
        this.fileSuffix = suffix;
        this.needUncompress = needUncompress;
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isNeedUncompress() {
        return needUncompress;
    }

    public String getDownloadUrl() {
        return HSConfigUtils.getRemoteContentDownloadURL() + remoteType + "/" + name + "/" + name + fileSuffix;
    }

    public String getDownloadItemFilePath() {     // /data/data/com.camera.beautycam/files/type/abc.zip
        return getDownloadItemFileStorePath() + fileSuffix;
    }

    public String getDownloadItemTempFilePath() { // /data/data/com.camera.beautycam/files/type/abc_temp.zip
        return getDownloadItemFileStorePath() + TEMP + fileSuffix;
    }

    public String getDownloadItemFileStorePath() {     // /data/data/com.camera.beautycam/files/type/abc
        return getFileRootFolderPath() + type + File.separator + name;
    }

    public String getDownloadItemFileStoreTempPath() { // /data/data/com.camera.beautycam/files/type/abc_temp
        return getDownloadItemFileStorePath() + TEMP;
    }

    public boolean isDownloaded() {
        File file = new File(getDownloadItemFilePath());
        return file.exists() && file.length() > 0;
    }

    private String getFileRootFolderPath() { // /data/data/com.camera.beautycam/files/
        return HSApplication.getContext().getFilesDir() + File.separator;
    }

    @Override
    public int hashCode() {
        if (name != null && type != null) {
            return 31 * name.hashCode() + type.hashCode();
        } else {
            return 17;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof DownloadItem)) {
            return false;
        } else {
            DownloadItem other = (DownloadItem) obj;
            return TextUtils.equals(other.getName(), name) && TextUtils.equals(other.getType(), type);
        }
    }
}

package com.ihs.inputmethod.uimodules.ui.sticker;

import android.os.Parcel;
import android.os.Parcelable;

import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.utils.GlideURIUtils;

/**
 * Created by yanxia on 2017/6/8.
 */

public class Sticker implements Parcelable{
    public static final String STICKER_IMAGE_PNG_SUFFIX = ".png";
    public static final String STICKER_IMAGE_GIF_SUFFIX = ".gif";

    private final String stickerUri;
    private final String stickerName;
    private final String stickerGroupName;
    private final String stickerRemoteUri;
    private final String stickerFileSuffix;//file suffix,extension,like ".png",".gif"
    private String filePath;

    public Sticker(String stickerUri) {
        this.stickerUri = stickerUri;
        int beginIndex = stickerUri.lastIndexOf("/");
        int endIndex = stickerUri.lastIndexOf(".");
        if (beginIndex != endIndex) {
            this.stickerName = stickerUri.substring(beginIndex + 1, endIndex);
            this.stickerFileSuffix = stickerUri.substring(endIndex);//get the extension
        } else {
            this.stickerName = stickerUri;
            if (endIndex != -1) {
                this.stickerFileSuffix = stickerUri.substring(endIndex);
            } else {
                this.stickerFileSuffix = "";//default is empty string.
            }

        }
        String[] stickerNameStrList = this.stickerName.split("-");
        stickerGroupName = stickerNameStrList[0];
        StringBuilder stringBuilder = new StringBuilder(getStickerDownloadBaseUrl())
                .append(stickerGroupName).append("/").append(stickerName).append(this.stickerFileSuffix);
        stickerRemoteUri = stringBuilder.toString();
    }


    protected Sticker(Parcel in) {
        stickerUri = in.readString();
        stickerName = in.readString();
        stickerGroupName = in.readString();
        stickerRemoteUri = in.readString();
        stickerFileSuffix = in.readString();
        filePath = in.readString();
    }

    public static final Creator<Sticker> CREATOR = new Creator<Sticker>() {
        @Override
        public Sticker createFromParcel(Parcel in) {
            return new Sticker(in);
        }

        @Override
        public Sticker[] newArray(int size) {
            return new Sticker[size];
        }
    };

    private String getStickerDownloadBaseUrl() {
        return HSConfig.getString("Application", "Server", "StickerDownloadBaseURL") + "/";
    }

    public String getStickerUri() {
        return stickerUri;
    }

    public boolean isAssetUri() {
        return stickerUri.startsWith(GlideURIUtils.Type.Assets.getStr());
    }

    public boolean isFileUri() {
        return stickerUri.startsWith(GlideURIUtils.Type.File.getStr());
    }

    public String getStickerName() {
        return stickerName;
    }

    public String getStickerGroupName() {
        return stickerGroupName;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public String getStickerRemoteUri() {
//        return stickerRemoteUri;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public String getStickerFileSuffix() {
        return stickerFileSuffix;
    }

    @Override
    public String toString() {
        return stickerUri;
    }

    @Override
    public int hashCode() {
        return stickerUri != null ? stickerUri.hashCode() : 17;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sticker sticker = (Sticker) obj;
        return stickerUri != null ? stickerUri.equals(sticker.stickerUri) : sticker.stickerUri == null;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.stickerUri);
        dest.writeString(this.stickerName);
        dest.writeString(this.stickerGroupName);
    }
}

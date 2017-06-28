package com.ihs.inputmethod.uimodules.ui.sticker;

import com.ihs.commons.config.HSConfig;

/**
 * Created by yanxia on 2017/6/8.
 */

public class Sticker {
    public static final String STICKER_IMAGE_PNG_SUFFIX = ".png";

    private final String stickerUri;
    private final String stickerName;
    private final String stickerGroupName;
    private final String stickerRemoteUri;

    public Sticker(String stickerUri) {
        this.stickerUri = stickerUri;
        int beginIndex = stickerUri.lastIndexOf("/");
        int endIndex = stickerUri.lastIndexOf(".");
        if (beginIndex != endIndex) {
            this.stickerName = stickerUri.substring(beginIndex + 1, endIndex);
        } else {
            this.stickerName = stickerUri;
        }
        String[] stickerNameStrList = this.stickerName.split("-");
        stickerGroupName = stickerNameStrList[0];
        StringBuilder stringBuilder = new StringBuilder(getStickerDownloadBaseUrl())
                .append(stickerGroupName).append("/").append(stickerName).append(STICKER_IMAGE_PNG_SUFFIX);
        stickerRemoteUri = stringBuilder.toString();
    }

    private String getStickerDownloadBaseUrl() {
        return HSConfig.getString("Application", "Server", "StickerDownloadBaseURL");
    }

    public String getStickerUri() {
        return stickerUri;
    }

    public String getStickerName() {
        return stickerName;
    }

    public String getStickerGroupName() {
        return stickerGroupName;
    }

    public String getStickerRemoteUri() {
        return stickerRemoteUri;
    }

    @Override
    public String toString() {
        return stickerUri;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sticker sticker = (Sticker) obj;
        return stickerUri != null ? stickerUri.equals(sticker.stickerUri) : sticker.stickerUri == null;
    }
}

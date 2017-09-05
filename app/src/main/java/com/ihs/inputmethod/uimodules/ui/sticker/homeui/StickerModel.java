package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class StickerModel {
    private StickerGroup stickerGroup;

    private boolean isDownloaded;

    private String stickerTag;

    public StickerModel(StickerGroup stickerGroup) {
        this.stickerGroup = stickerGroup;
        isDownloaded = false;
        stickerTag = null;
    }

    public StickerGroup getStickerGroup() {
        return stickerGroup;
    }

    public String getStickerTag() {
        return stickerTag;
    }

    public void setStickTag(String s) {
        stickerTag = s;
    }

    public boolean getIsDownload() {
        return isDownloaded;
    }

    public void setIsDownloaded(boolean download) {
        isDownloaded = download;
    }
}

package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class StickerModel {
    private StickerGroup stickerGroup;

    private boolean isDownloaded;

    private String stickerTag;

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public StickerModel(StickerGroup stickerGroup) {
//        this.stickerGroup = stickerGroup;
//        isDownloaded = false;
//        stickerTag = null;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public StickerGroup getStickerGroup() {
//        return stickerGroup;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public String getStickerTag() {
//        return stickerTag;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void setStickTag(String s) {
//        stickerTag = s;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public boolean getIsDownload() {
//        return isDownloaded;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void setIsDownloaded(boolean download) {
//        isDownloaded = download;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StickerModel that = (StickerModel) o;

        return stickerGroup.equals(that.stickerGroup);

    }

    @Override
    public int hashCode() {
        return stickerGroup.hashCode();
    }
}

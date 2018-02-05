package com.ihs.inputmethod.uimodules.ui.sticker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanxia on 2017/6/21.
 */

public class StickerPanelItemGroup {
    private final String stickerPanelItemGroupName;
    private List<StickerPanelItem> stickerPanelItems = new ArrayList<>();

    public StickerPanelItemGroup(String stickerPanelItemGroupName) {
        this.stickerPanelItemGroupName = stickerPanelItemGroupName;
    }

    public String getStickerPanelItemGroupName() {
        return stickerPanelItemGroupName;
    }

    public List<StickerPanelItem> getStickerPanelItemList() {
        if (stickerPanelItems == null) {
            stickerPanelItems = new ArrayList<>();
        }
        return stickerPanelItems;
    }

    public void addStickerPanelItem(final StickerPanelItem stickerPanelItem) {
        stickerPanelItems.add(stickerPanelItem);
    }

    public void removeLastStickerPanelItem() {
        stickerPanelItems.remove(stickerPanelItems.size() - 1);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void removeStickerPanelItem(final StickerPanelItem stickerPanelItem) {
//        stickerPanelItems.remove(stickerPanelItem);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public int size() {
        return stickerPanelItems.size();
    }

    public void clear() {
        stickerPanelItems.clear();
    }
}

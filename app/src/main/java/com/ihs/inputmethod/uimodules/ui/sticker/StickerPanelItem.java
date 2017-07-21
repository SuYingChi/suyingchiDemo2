package com.ihs.inputmethod.uimodules.ui.sticker;

/**
 * Created by yanxia on 2017/6/20.
 */

public class StickerPanelItem {
    public static final int ITEM_TYPE_STICKER = 0;
    public static final int ITEM_TYPE_PLACE_HOLDER = 1;
    public static final int ITEM_TYPE_DIVIDER = 2;
    public static StickerPanelItem PLACEHOLDER = new StickerPanelItem(ITEM_TYPE_PLACE_HOLDER);
    public static StickerPanelItem DIVIDER = new StickerPanelItem(ITEM_TYPE_DIVIDER);
    private Sticker sticker;
    private int viewType;

    public StickerPanelItem(int itemType) {
        this(itemType, null);
    }

    public StickerPanelItem(Sticker sticker) {
        this(ITEM_TYPE_STICKER, sticker);
    }

    private StickerPanelItem(int itemType, Sticker sticker) {
        this.viewType = itemType;
        this.sticker = sticker;
    }

    public boolean isDivider() {
        return viewType == ITEM_TYPE_DIVIDER;
    }

    public boolean isPlaceHolder() {
        return viewType == ITEM_TYPE_PLACE_HOLDER;
    }

    public boolean isSticker() {
        return viewType == ITEM_TYPE_STICKER;
    }

    public Sticker getSticker() {
        return sticker;
    }

    public void setSticker(Sticker sticker) {
        this.sticker = sticker;
    }

}

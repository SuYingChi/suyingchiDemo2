package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate.StickerMyCardAdapterDelegate;


/**
 * Created by guonan.lv on 17/8/10.
 */

public class MyStickerAdapter extends CommonStickerAdapter {

    public MyStickerAdapter() {
        super(null);
        delegatesManager.addDelegate(new StickerMyCardAdapterDelegate());
    }
}

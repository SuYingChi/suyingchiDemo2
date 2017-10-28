package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate.StickerBigCreateFacemojiAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate.StickerFacemojiAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate.StickerHomeCardAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate.StickerMoreComingAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate.StickerSamllCreateFacemojiAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate.StickerTitleAdapterDelegate;


/**
 * Created by guonan.lv on 17/8/10.
 */

public class HomeStickerAdapter extends CommonStickerAdapter {
    private StickerFacemojiAdapterDelegate stickerFacemojiAdapterDelegate;

    public HomeStickerAdapter(OnStickerItemClickListener onStickerItemClickListener) {
        super();
        stickerFacemojiAdapterDelegate = new StickerFacemojiAdapterDelegate(onStickerItemClickListener);
        delegatesManager.addDelegate(new StickerTitleAdapterDelegate())
                .addDelegate(new StickerBigCreateFacemojiAdapterDelegate())
                .addDelegate(new StickerSamllCreateFacemojiAdapterDelegate())
                .addDelegate(stickerFacemojiAdapterDelegate)
                .addDelegate(new StickerMoreComingAdapterDelegate())
                .addDelegate(new StickerHomeCardAdapterDelegate(onStickerItemClickListener));
    }

    public void setPlayStickerFacemoij(boolean playAnim){
        if (stickerFacemojiAdapterDelegate != null){
            stickerFacemojiAdapterDelegate.setPlayAnim(playAnim);
        }
    }
}

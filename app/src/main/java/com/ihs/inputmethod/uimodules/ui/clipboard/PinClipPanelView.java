package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ihs.inputmethod.uimodules.ui.common.adapter.RecentClipboardAdapter;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class PinClipPanelView extends FrameLayout implements RecentClipboardAdapter.SaveRecentItemToPins {

    private PinsClipboardPresenter pinsClipPresenter;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        pinsClipPresenter= new PinsClipboardPresenter(this);

    }

    public PinClipPanelView(Context context) {
        super(context);
    }

    public PinClipPanelView(Context context,  AttributeSet attrs) {
        super(context, attrs);
    }

    public PinClipPanelView( Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void saveToPins(String itemPinsContent) {
        pinsClipPresenter.addDataAndFresh(itemPinsContent);
    }
}

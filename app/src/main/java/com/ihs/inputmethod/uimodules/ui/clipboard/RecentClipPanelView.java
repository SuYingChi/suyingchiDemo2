package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ihs.inputmethod.uimodules.ui.common.adapter.PinsClipPanelViewAdapter;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class RecentClipPanelView extends FrameLayout  {

    ClipboardPresenter clipboardPresenter;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        clipboardPresenter = ClipboardPresenter.getInstance();
        clipboardPresenter.setRecentClipPanelView(this);
        ClipboardMonitor.getInstance().registerClipboardMonitor(clipboardPresenter);
    }

    public RecentClipPanelView(Context context) {
        this(context,null);
    }

    public RecentClipPanelView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RecentClipPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

}

package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.PinsClipPanelViewAdapter;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class PinClipPanelView extends RecyclerView {

    private ClipboardPresenter clipPresenter;
    private RecyclerView recyclerView;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        clipPresenter = ClipboardPresenter.getInstance();
        clipPresenter.setPinClipPanelView(this);
        recyclerView = findViewById(R.id.clipboard_Pins_content);
        recyclerView.setAdapter(clipPresenter.getPinsClipPanelViewAdapter());
    }

    public PinClipPanelView(Context context) {
            this(context, null);
        }

    public PinClipPanelView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

    public PinClipPanelView(Context context, AttributeSet attrs, int defStyleAttr){
            super(context, attrs, defStyleAttr);
        }


}
package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class ClipboardPanelPinsView extends RecyclerView {


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public ClipboardPanelPinsView(Context context) {
            this(context, null);
        }

    public ClipboardPanelPinsView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

    public ClipboardPanelPinsView(Context context, AttributeSet attrs, int defStyleAttr){
            super(context, attrs, defStyleAttr);
        }


}
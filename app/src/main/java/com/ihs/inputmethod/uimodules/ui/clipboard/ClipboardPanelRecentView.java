package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class ClipboardPanelRecentView extends RecyclerView {


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public ClipboardPanelRecentView(Context context) {
        this(context,null);
    }

    public ClipboardPanelRecentView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ClipboardPanelRecentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

}

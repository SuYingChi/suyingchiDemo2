package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class ClipboardPanelView extends FrameLayout {

    public ClipboardPanelView( Context context) {
        this(context,null);
    }

    public ClipboardPanelView( Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ClipboardPanelView( Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }
}

package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by guonan.lv on 17/9/5.
 */

public abstract class LoadFootView extends RelativeLayout {

    public LoadFootView(Context context) {
        this(context, null);
    }

    public LoadFootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadFootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public abstract void reset();
}

package com.ihs.inputmethod.feature.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.inputmethod.uimodules.R;


public class CustomRootView extends FrameLayout {

    public CustomRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        View containerView = ViewUtils.findViewById(this, R.id.container_view);
        int bottomMargin = CommonUtils.getNavigationBarHeight(getContext());
        ViewUtils.setMargins(containerView, 0, 0, 0, bottomMargin);
    }
}
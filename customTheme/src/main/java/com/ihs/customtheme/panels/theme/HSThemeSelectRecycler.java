package com.ihs.customtheme.panels.theme;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.ihs.inputmethod.api.HSInputMethodCommonUtils;
import com.ihs.inputmethod.api.HSInputMethodTheme;

public class HSThemeSelectRecycler extends RecyclerView {

    public HSThemeSelectRecycler(Context context) {
        super(context);
    }

    public HSThemeSelectRecycler(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public HSThemeSelectRecycler(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = HSInputMethodCommonUtils.getDefaultKeyboardWidth();
        final int height = HSInputMethodCommonUtils.getDefaultKeyboardHeight();
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.setBackgroundColor(HSInputMethodTheme.getThemeMainColor());
    }
}

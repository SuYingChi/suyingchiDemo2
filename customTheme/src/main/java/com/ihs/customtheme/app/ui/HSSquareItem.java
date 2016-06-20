package com.ihs.customtheme.app.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class HSSquareItem extends LinearLayout {
    public HSSquareItem(Context context) {
        this(context,null);
    }

    public HSSquareItem(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HSSquareItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}

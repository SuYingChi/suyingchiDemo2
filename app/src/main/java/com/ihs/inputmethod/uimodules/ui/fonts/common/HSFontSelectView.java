package com.ihs.inputmethod.uimodules.ui.fonts.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ListView;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;

public class HSFontSelectView extends ListView {
    private Drawable mItemDefaultBackgroundDrawable;
    private Drawable mItemSelectedBackground;
    private final int fontItemTextColor;
    private final int fontItemDividerColor;

    public HSFontSelectView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSFontSelectView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs, com.ihs.inputmethod.R.styleable.FontSelectPannel, defStyle, com.ihs.inputmethod.R.style.KeyboardView);
//        fontItemTextColor = keyboardViewAttr.getColor(R.styleable.FontSelectPannel_fontTextColor, 0);
//        fontItemDividerColor = keyboardViewAttr.getColor(R.styleable.FontSelectPannel_fontDividerColor, 0);

        fontItemDividerColor = HSKeyboardThemeManager.getCurrentTheme().isDarkBg() ? Color.parseColor("#33ffffff") : Color.parseColor("#33000000");
        fontItemTextColor = Color.parseColor("#ff181818");

        mItemDefaultBackgroundDrawable = new ColorDrawable(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
        mItemSelectedBackground = mItemDefaultBackgroundDrawable.getConstantState().newDrawable();
        mItemSelectedBackground.mutate().setAlpha(204);
        setDivider(new ColorDrawable(fontItemDividerColor));
        setDividerHeight(HSDisplayUtils.dip2px(1));
        setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = HSResourceUtils.getDefaultKeyboardWidth(getResources());
        final int height = HSResourceUtils.getDefaultKeyboardHeight(getResources());
        setMeasuredDimension(width, height);
    }

    public Drawable getItemDefaultBackground() {
        return mItemDefaultBackgroundDrawable;
    }

    public int getItemDividerColor() {
        return fontItemDividerColor;
    }

    public int getItemTextColor() {
        return fontItemTextColor;
    }

    public Drawable getItemSelectedBackground() {
        return mItemSelectedBackground;
    }

    public void onDismiss() {
        ((HSFontSelectViewAdapter) this.getAdapter()).cancelAnimation();
    }
}


package com.keyboard.inputmethod.panels.theme;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ListView;

import com.ihs.inputmethod.api.HSInputMethodCommonUtils;
import com.ihs.inputmethod.api.HSInputMethodTheme;

public class HSThemeSelectView extends ListView {
    private final int fontItemDividerColor;
    private Drawable mBackground;

    public HSThemeSelectView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSThemeSelectView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs, com.ihs.inputmethod.R.styleable.FontSelectPannel, defStyle, com.ihs.inputmethod.R.style.KeyboardView);
        fontItemDividerColor = keyboardViewAttr.getColor(com.ihs.inputmethod.R.styleable.FontSelectPannel_fontDividerColor, 0);
        this.setBackgroundColor(HSInputMethodTheme.getThemeMainColor());
        mBackground = getBackground();
        setDivider(new ColorDrawable(fontItemDividerColor));
        setDividerHeight(1);

    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final Resources res = getContext().getResources();
        final int width = HSInputMethodCommonUtils.getDefaultKeyboardWidth();
        final int height = HSInputMethodCommonUtils.getDefaultKeyboardHeight();
        setMeasuredDimension(width, height);
    }

    public int getItemDividerColor() {
        return fontItemDividerColor;
    }

    public Drawable getItemDefaultBackground() {
        return mBackground;
    }

}

package com.keyboard.inputmethod.panels.fonts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ListView;

import com.ihs.inputmethod.api.HSInputMethodCommonUtils;
import com.ihs.inputmethod.api.HSInputMethodTheme;

public class HSFontSelectView extends ListView {
    
    private Drawable mBackground;
    private Drawable mItemSelectedBackground;
    private final int fontItemTextColor;
    private final int fontItemDividerColor;
    
    public HSFontSelectView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSFontSelectView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs, com.ihs.inputmethod.R.styleable.FontSelectPannel, defStyle, com.ihs.inputmethod.R.style.KeyboardView);
        fontItemTextColor = keyboardViewAttr.getColor(com.ihs.inputmethod.R.styleable.FontSelectPannel_fontTextColor, 0);
        fontItemDividerColor = keyboardViewAttr.getColor(com.ihs.inputmethod.R.styleable.FontSelectPannel_fontDividerColor, 0);
        this.setBackgroundColor(HSInputMethodTheme.getThemeMainColor());
        mBackground = getBackground();
        mItemSelectedBackground = mBackground.getConstantState().newDrawable();
        mItemSelectedBackground.mutate().setAlpha(204);
        setDivider(new ColorDrawable(fontItemDividerColor));
        setDividerHeight(1);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = HSInputMethodCommonUtils.getDefaultKeyboardWidth();
        final int height = HSInputMethodCommonUtils.getDefaultKeyboardHeight();
        setMeasuredDimension(width, height);
    }
    
    public Drawable getItemDefaultBackground() {
      return mBackground;
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


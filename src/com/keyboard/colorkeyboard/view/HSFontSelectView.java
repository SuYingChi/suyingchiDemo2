package com.keyboard.colorkeyboard.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ListView;

import com.ihs.inputmethod.extended.theme.HSKeyboardThemeManager;
import com.keyboard.colorkeyboard.R;
import com.ihs.inputmethod.latin.utils.ResourceUtils;

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
        final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs, R.styleable.FontSelectPannel, defStyle, R.style.KeyboardView);
        fontItemTextColor = keyboardViewAttr.getColor(R.styleable.FontSelectPannel_fontTextColor, 0);
        fontItemDividerColor = keyboardViewAttr.getColor(R.styleable.FontSelectPannel_fontDividerColor, 0);
        this.setBackgroundColor(HSKeyboardThemeManager.getDominantColor());
        mBackground = getBackground();
        mItemSelectedBackground = mBackground.getConstantState().newDrawable();
        mItemSelectedBackground.mutate().setAlpha(204);
        setDivider(new ColorDrawable(fontItemDividerColor));
        setDividerHeight(1);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final Resources res = getContext().getResources();
        final int width = ResourceUtils.getDefaultKeyboardWidth(res);
        final int height = ResourceUtils.getDefaultKeyboardHeight(res);
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


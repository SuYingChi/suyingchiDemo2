package com.smartkeyboard.rainbow.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ListView;

import com.ihs.inputmethod.extended.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.latin.R;
import com.ihs.inputmethod.latin.utils.ResourceUtils;

public class HSThemeSelectView extends ListView {
    private final int fontItemTextColor;
    private final int fontItemDividerColor;
    private Drawable mBackground;

    public HSThemeSelectView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSThemeSelectView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs, R.styleable.FontSelectPannel, defStyle, R.style.KeyboardView);
        fontItemTextColor = keyboardViewAttr.getColor(R.styleable.FontSelectPannel_fontTextColor, 0);
        fontItemDividerColor = keyboardViewAttr.getColor(R.styleable.FontSelectPannel_fontDividerColor, 0);
        this.setBackgroundColor(HSKeyboardThemeManager.getDominantColor());
        mBackground = getBackground();
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

    public int getItemDividerColor() {
        return fontItemDividerColor;
    }

    public int getItemTextColor() {
        return fontItemTextColor;
    }

    public Drawable getItemDefaultBackground() {
        return mBackground;
    }

}

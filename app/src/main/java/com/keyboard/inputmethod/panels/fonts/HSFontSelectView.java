package com.keyboard.inputmethod.panels.fonts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ListView;

import com.ihs.inputmethod.api.HSInputMethodCommonUtils;
import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.ihs.inputmethod.theme.HSKeyboardThemeManager;

public class HSFontSelectView extends ListView {

    private Drawable mBackground;
    private Drawable mItemSelectedBackground;
    private final int fontItemTextColor;
    private int fontItemDividerColor;

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

        fontItemDividerColor = getColorWithoutAlpha(HSInputMethodTheme.getThemeMainColor(), fontItemDividerColor);
        setDivider(new ColorDrawable(fontItemDividerColor));
        HSKeyboardThemeManager.setmFontDividerColor(fontItemDividerColor);
        setDividerHeight(1);
        setCacheColorHint(fontItemDividerColor);
    }

    enum ColorType {Red, Blue, Green}


    private int getColorWithoutAlpha(int backgroundColor, int frontColor) {
        //REF:http://406625590.blog.163.com/blog/static/335305972013113144438381/
        int red = getMixedColor(backgroundColor, frontColor, ColorType.Red);
        int blue = getMixedColor(backgroundColor, frontColor, ColorType.Blue);
        int green = getMixedColor(backgroundColor, frontColor, ColorType.Green);

        return Color.rgb(red,blue,green);
    }

    private int getMixedColor(int backgroundColor, int frontColor, ColorType type) {
        int color1 = 0;
        int color2 = 0;
        switch (type) {
            case Red:
                color1 = Color.red(backgroundColor);
                color2 = Color.red(frontColor);
                break;
            case Blue:
                color1 = Color.blue(backgroundColor);
                color2 = Color.blue(frontColor);
                break;
            case Green:
                color1 = Color.green(backgroundColor);
                color2 = Color.green(frontColor);
                break;
        }
        float alpha = getAlpha(frontColor);
        return (int) (color1 * (1 - alpha) + (color2 * alpha));
    }

    private float getAlpha(int color) {
        ColorDrawable cd = new ColorDrawable(color);
        int alpha = cd.getAlpha();
        return alpha/255.0f;
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


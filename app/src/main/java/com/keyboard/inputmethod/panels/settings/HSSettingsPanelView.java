package com.keyboard.inputmethod.panels.settings;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.ihs.inputmethod.api.HSInputMethodCommonUtils;
import com.ihs.inputmethod.api.HSInputMethodTheme;

public class HSSettingsPanelView extends ScrollView {

    private final int itemTextColor;

    public HSSettingsPanelView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSSettingsPanelView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs, com.ihs.inputmethod.R.styleable.SettingsPannel, defStyle, com.ihs.inputmethod.R.style.KeyboardView);
        itemTextColor = keyboardViewAttr.getColor(com.ihs.inputmethod.R.styleable.SettingsPannel_settingsTextColor, 0);
        this.setBackgroundColor(HSInputMethodTheme.getThemeMainColor());
    }

    public int getSettingsItemTextColor() {
        return itemTextColor;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final Resources res = getContext().getResources();
        final int width = HSInputMethodCommonUtils.getDefaultKeyboardWidth();
        final int height = HSInputMethodCommonUtils.getDefaultKeyboardHeight();
        setMeasuredDimension(width, height);
    }

}

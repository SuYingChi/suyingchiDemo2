package com.ihs.inputmethod.uimodules.ui.settings.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.api.utils.HSResourceUtils;

public class HSSettingsPanelView extends ScrollView {

    private final int itemTextColor;

    public HSSettingsPanelView(final Context context, final AttributeSet attrs) {
       // this(context, attrs, 0);
        this(context, attrs, R.attr.stickerPalettesViewStyle);
    }

    public HSSettingsPanelView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs, com.ihs.inputmethod.R.styleable.SettingsPannel, defStyle, com.ihs.inputmethod.R.style.KeyboardView);
        itemTextColor = keyboardViewAttr.getColor(com.ihs.inputmethod.R.styleable.SettingsPannel_settingsTextColor, 0);
        keyboardViewAttr.recycle();
        this.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
    }

    public int getSettingsItemTextColor() {
        return itemTextColor;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = HSResourceUtils.getDefaultKeyboardWidth(getResources());
        final int height = HSResourceUtils.getDefaultKeyboardHeight(getResources());
        setMeasuredDimension(width, height);
    }

}

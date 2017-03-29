package com.mobipioneer.inputmethod.panels.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.ihs.inputmethod.R;

public class HSSettingsPanelView extends ScrollView {

    private final int itemTextColor;

    public HSSettingsPanelView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSSettingsPanelView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs, R.styleable.SettingsPannel, defStyle, R.style.KeyboardView);
        itemTextColor = keyboardViewAttr.getColor(R.styleable.SettingsPannel_settingsTextColor, 0);
        keyboardViewAttr.recycle();
        //// TODO: 16/12/9  getMain
//        this.setBackgroundColor(HSInputMethodTheme.getThemeMainColor());
    }

    public int getSettingsItemTextColor() {
        return itemTextColor;
    }

//    @Override
//    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        final int width = HSInputMethodCommonUtils.getDefaultKeyboardWidth();
//        final int height = HSInputMethodCommonUtils.getDefaultKeyboardHeight();
//        setMeasuredDimension(width, height);
//    }
}

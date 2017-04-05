package com.ihs.inputmethod.uimodules.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.BaseFunction;

import static com.ihs.inputmethod.uimodules.BaseFunctionBar.getFuncButtonDrawable;

/**
 * Created by chenyuanming on 12/10/2016.
 */

public class SettingsButton extends AppCompatImageView implements BaseFunction.NewTipStatueChangeListener {

    public SettingsButton(Context context) {
        super(context);
        init();
    }

    public SettingsButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        refreshDrawable();
    }


    private static final String MENU_DRAWABLE = HSKeyboardThemeManager.IMG_MENU_FUNCTION;
    private static final String BACK_DRAWABLE = HSKeyboardThemeManager.IMG_MENU_BACK;


    public static class SettingButtonType {
        public final static int MENU = 1;
        public final static int SETTING = 2;
        public final static int BACK = 4;
    }

    private int buttonType = SettingButtonType.MENU;

    public void setButtonType(int imageType) {
        this.buttonType = imageType;
        refreshDrawable();
        setEnabled(true);
    }

    public int getButtonType() {
        return buttonType;
    }

    private void refreshDrawable() {
        String drawableName;
        switch (buttonType) {
            case SettingButtonType.MENU:
                drawableName = MENU_DRAWABLE;
                break;

            case SettingButtonType.BACK:
            case SettingButtonType.SETTING:
                drawableName = BACK_DRAWABLE;
                break;

            default:
                throw new IllegalArgumentException("set setting button type wrong !");
        }


        Drawable drawable = HSKeyboardThemeManager.getThemeSettingMenuDrawable(drawableName, null);
        if (drawable == null) {
            drawable = VectorDrawableCompat.create(getResources(), getDrawableFromResources(drawableName), null);
            drawable = getTintDrawable(drawable);
        }
        setImageDrawable(drawable);
    }

    @NonNull
    private Drawable getTintDrawable(Drawable drawable) {
        return getFuncButtonDrawable(drawable);
    }

    public int getDrawableFromResources(String resName) {
        if (resName.contains(".")) {
            resName = resName.substring(0, resName.indexOf("."));
        }
        return HSApplication.getContext().getResources().getIdentifier(resName, "drawable", HSApplication.getContext().getPackageName());
    }

    @Override
    public boolean shouldShowTip() {
        return buttonType == SettingButtonType.MENU;
    }
}

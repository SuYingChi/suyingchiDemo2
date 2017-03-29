package com.ihs.inputmethod.uimodules.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.BaseFunctionBar;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by chenyuanming on 12/10/2016.
 */

public class ClothButton extends AppCompatImageView {

    private static final String IMG_MENU_CLOTH = "menu_cloth.png";

    public ClothButton(Context context) {
        super(context);
        init();
    }

    public ClothButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClothButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        refreshDrawable();
    }

    private void refreshDrawable() {
        String drawableName = IMG_MENU_CLOTH;

        Drawable drawable = HSKeyboardThemeManager.getThemeSettingMenuDrawable(drawableName, null);
        if (drawable == null) {
            drawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_menu_cloth, null);
            drawable = getTintDrawable(drawable);
        }
        setImageDrawable(drawable);
    }

    @NonNull
    private Drawable getTintDrawable(Drawable drawable) {
        return BaseFunctionBar.getFuncButtonDrawable(drawable);
    }

}

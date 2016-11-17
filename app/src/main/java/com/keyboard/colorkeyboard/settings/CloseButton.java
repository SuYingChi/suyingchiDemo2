package com.keyboard.colorkeyboard.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.KeyboardPluginManager;
import com.ihs.inputmethod.uimodules.panel.HSKeyboardPanel;
import com.keyboard.colorkeyboard.MasterKeyboardPluginManager;

/**
 * Created by chenyuanming on 12/10/2016.
 */

public abstract class CloseButton extends ActionButton {
    private static final String PRESSED_DRAWABLE = "menu_back.png";
    private static final String NORMAL_DRAWABLE = "menu_back.png";


    public CloseButton(Context context) {
        super(context);
    }

    public CloseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CloseButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        MasterKeyboardPluginManager.getInstance().addActionButtonToStack(this);
    }
    @Override
    protected Drawable getPressedDrawable() {
        return HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources(PRESSED_DRAWABLE);
    }

    @Override
    protected Drawable getNormalDrawable() {
        return HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources(NORMAL_DRAWABLE);
    }



    @Override
    protected View.OnClickListener getOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MasterKeyboardPluginManager.getInstance().removeActionButtonToStack(CloseButton.this);
                getPanel().dismiss();
                KeyboardPluginManager.getInstance().showPanel(HSKeyboardPanel.KEYBOARD_PANEL_KEYBOARD_NAME);
            }
        };
    }
}

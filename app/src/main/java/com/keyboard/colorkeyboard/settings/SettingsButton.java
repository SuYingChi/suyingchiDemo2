package com.keyboard.colorkeyboard.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.KeyboardPluginManager;
import com.ihs.inputmethod.uimodules.panel.HSKeyboardPanel;
import com.ihs.inputmethod.uimodules.ui.keyboard.KeyboardPanel;
import com.keyboard.colorkeyboard.KeyboardPanelManager;
import com.keyboard.colorkeyboard.R;

/**
 * Created by chenyuanming on 12/10/2016.
 */

public class SettingsButton extends ActionButton {
    private boolean isSelected;
    private HSKeyboardPanel panel;

    public SettingsButton(Context context) {
        super(context);
    }

    public SettingsButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingsButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private static final String PRESSED_DRAWABLE = "menu_back.png";

    private static final String NORMAL_DRAWABLE = "menu_setting.png";

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
                isSelected = !isSelected;
                setSelected(isSelected);
            }
        };
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        isSelected = selected;
        onButtonStateChanged(selected);
    }

    @Override
    public HSKeyboardPanel getPanel() {
        if (panel == null) {
            panel = KeyboardPluginManager.getInstance().getPanel(HSApplication.getContext().getString(R.string.panel_settings));
            panel.show();
        }
        return panel;
    }

    public void onButtonStateChanged(boolean isSelected) {
//        View panelView = getPanel().getPanelView();
//        if (panelView != null) {
//            if (isSelected) {
//                expandOrCollapse(panelView, true);
//                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_settings_click");
//            } else {
//                expandOrCollapse(panelView, false);
//            }
//        }
        if (KeyboardPanelManager.getInstance().getKeyboardPanelSwitchContainer().getCurrentPanel() != null) {
            if(isSelected ) {
                KeyboardPanelManager.getInstance().getKeyboardPanelSwitchContainer().getCurrentPanel().showChildPanel(HSSettingsPanel.class);
            }else {
                ((HSKeyboardPanel) KeyboardPanelManager.getInstance().getKeyboardPanelSwitchContainer().getCurrentPanel()).getOnPanelChangedListener().backToParentPanel(false);

            }
        }

    }

    private void expandOrCollapse(final View v, final boolean isExpand) {
        if (v == null) {
            return;
        }
        if(isExpand) {
            KeyboardPanelManager.getInstance().getKeyboardPanelSwitchContainer().getCurrentPanel().showChildPanel(HSSettingsPanel.class);
        }else {
            KeyboardPanelManager.getInstance().getKeyboardPanelSwitchContainer().showPanel(KeyboardPanel.class);
        }
    }


    public void setViewHeight(View v, int height) {
        if (v != null && v.getLayoutParams() != null) {
            final ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = height;
            v.requestLayout();
        }
    }
}

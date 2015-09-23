package com.smartkeyboard.rainbow.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import com.ihs.inputmethod.extended.api.HSKeyboard;
import com.ihs.inputmethod.extended.api.HSKeyboardPanel;
import com.ihs.inputmethod.extended.api.HSKeyboardPanel.IHSKeyboardPanelListener;
import com.ihs.inputmethod.extended.theme.HSKeyboardThemeManager;
import com.smartkeyboard.rainbow.R;

public class HSThemeSelectPanel {

    public HSThemeSelectPanel(Context context) {
        mContext = context;
    }

    private Context mContext;

    private HSThemeSelectView mThemeSelectView;
    private HSThemeSelectViewAdapter mAdapter;

    private IHSKeyboardPanelListener listener = new IHSKeyboardPanelListener() {

        @Override
        public void onShow() {
        }

        @Override
        public void onDismiss() {
        }

        @Override
        public void onCreatePanelView() {
            initFontSelectView();
            HSKeyboard.getInstance().setPanelView(HSKeyboardPanel.KEYBOARD_PANEL_THEME, mThemeSelectView);
        }
    };


    public void init() {
        Drawable icon = HSKeyboardThemeManager.getStyledAssetDrawable(null, HSKeyboardThemeManager.TABBAR_THEME_ICON);
        Drawable iconSelected = HSKeyboardThemeManager.getStyledAssetDrawable(null, HSKeyboardThemeManager.TABBAR_THEME_ICON_CHOSED);
        mThemeSelectView = null;
        HSKeyboard.getInstance().addPanelView(new HSKeyboardPanel(icon, iconSelected, mThemeSelectView, HSKeyboardPanel.KEYBOARD_PANEL_THEME, listener));
    }

    private void initFontSelectView() {
        Context mThemeContext = new ContextThemeWrapper(mContext, HSKeyboardThemeManager.getCurrentTheme().mStyleId);
        LayoutInflater inflater = LayoutInflater.from(mThemeContext);
        mThemeSelectView = (HSThemeSelectView) inflater.inflate(R.layout.theme_select_layout, null);
        mAdapter = new HSThemeSelectViewAdapter(mContext, mThemeSelectView);
        mThemeSelectView.setAdapter(mAdapter);
    }
}

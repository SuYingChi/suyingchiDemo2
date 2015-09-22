package com.smartkeyboard.rainbow.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.extended.api.HSKeyboard;
import com.ihs.inputmethod.extended.api.HSKeyboardPanel;
import com.ihs.inputmethod.extended.api.HSKeyboardPanel.IHSKeyboardPanelListener;
import com.ihs.inputmethod.extended.theme.HSKeyboardThemeManager;
import com.smartkeyboard.rainbow.R;

public class HSFontSelectPanel {

    public HSFontSelectPanel(Context context) {
        mContext = context;
    }

    private Context mContext;

    private HSFontSelectView mFontSelectView;
    private HSFontSelectViewAdapter mAdapter;

    private IHSKeyboardPanelListener listener = new IHSKeyboardPanelListener() {

        @Override
        public void onShow() {
        }

        @Override
        public void onDismiss() {
            if (mAdapter != null) {
                mAdapter.cancelAnimation();
            }
        }
        
        @Override
        public void onCreatePanelView() {
            initFontSelectView();
            HSKeyboard.getInstance().setPanelView(HSKeyboardPanel.KEYBOARD_PANEL_FONT, mFontSelectView);
        }
    };
    
    private INotificationObserver loadDataObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle notificaiton) {
            if (eventName.equals(HSKeyboard.HS_NOTIFICATION_FONT_PANEL_DATA_LOADED)) {
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    public void init() {
        Drawable icon = HSKeyboardThemeManager.getStyledAssetDrawable(null,
                HSKeyboardThemeManager.TABBAR_FONTS_ICON);
        Drawable iconSelected = HSKeyboardThemeManager.getStyledAssetDrawable(null, HSKeyboardThemeManager.TABBAR_FONTS_ICON_CHOSED);

        mFontSelectView = null;
        HSKeyboard.getInstance().addPanelView(new HSKeyboardPanel(icon, iconSelected, mFontSelectView, HSKeyboardPanel.KEYBOARD_PANEL_FONT, listener));
    
        HSGlobalNotificationCenter.addObserver(HSKeyboard.HS_NOTIFICATION_FONT_PANEL_DATA_LOADED, loadDataObserver);
    }
    

    private void initFontSelectView() {
        Context mThemeContext = new ContextThemeWrapper(mContext, HSKeyboardThemeManager.getCurrentTheme().mStyleId);
        LayoutInflater inflater = LayoutInflater.from(mThemeContext);
        mFontSelectView = (HSFontSelectView)inflater.inflate(R.layout.font_select_layout, null);
        mAdapter = new HSFontSelectViewAdapter(mContext, mFontSelectView);
        mFontSelectView.setAdapter(mAdapter);
    }
    
}

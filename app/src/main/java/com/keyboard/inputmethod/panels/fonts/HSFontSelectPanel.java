package com.keyboard.inputmethod.panels.fonts;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSInputMethodPanel;
import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.keyboard.rainbow.R;

public class HSFontSelectPanel extends HSInputMethodPanel {


    public static final String PANEL_NAME_FONT = "fonts";

    public HSFontSelectPanel() {
        super(PANEL_NAME_FONT);
        mContext = HSApplication.getContext();
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_FONT_PANEL_DATA_LOADED, loadDataObserver);
    }


    private Context mContext;

    private HSFontSelectView mFontSelectView;
    private HSFontSelectViewAdapter mAdapter;

    private INotificationObserver loadDataObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle notificaiton) {
            if (eventName.equals(HSInputMethod.HS_NOTIFICATION_FONT_PANEL_DATA_LOADED)) {
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public View onCreatePanelView() {
        Context mThemeContext = new ContextThemeWrapper(mContext, HSInputMethodTheme.getCurrentThemeStyleId());
        LayoutInflater inflater = LayoutInflater.from(mThemeContext);
        mFontSelectView = (HSFontSelectView)inflater.inflate(R.layout.font_select_layout, null);
        mAdapter = new HSFontSelectViewAdapter(mContext, mFontSelectView);
        mFontSelectView.setAdapter(mAdapter);
        return mFontSelectView;
    }


    @Override
    public void onDestroyPanelView(){
        super.onDestroyPanelView();
        HSGlobalNotificationCenter.removeObserver(loadDataObserver);
    }

    
}

package com.ihs.inputmethod.uimodules.ui.fonts.common;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.BaseFunctionBar;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.panelcontainer.BasePanel;
import com.ihs.panelcontainer.panel.KeyboardPanel;

public class HSFontSelectPanel extends BasePanel {

    public HSFontSelectPanel() {
        mContext = HSApplication.getContext();
        HSGlobalNotificationCenter.addObserver(HSSpecialCharacterManager.HS_NOTIFICATION_SPECIAL_CHARACTERS_LOAD_FINISHED, loadDataObserver);
    }


    private Context mContext;

    private HSFontSelectViewAdapter mAdapter;

    private INotificationObserver loadDataObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle notificaiton) {
            if (eventName.equals(HSSpecialCharacterManager.HS_NOTIFICATION_SPECIAL_CHARACTERS_LOAD_FINISHED)) {
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public View onCreatePanelView() {
        //set functionBar setting button type
        BaseFunctionBar functionBar = (BaseFunctionBar) panelActionListener.getBarView();
        functionBar.setSettingButtonType(SettingsButton.SettingButtonType.BACK);

        Context mThemeContext = new ContextThemeWrapper(mContext, HSKeyboardThemeManager.getCurrentTheme().mStyleId);
        LayoutInflater inflater = LayoutInflater.from(mThemeContext);
        HSFontSelectView mFontSelectView = (HSFontSelectView) inflater.inflate(R.layout.panel_font_select_layout, null);
        mAdapter = new HSFontSelectViewAdapter(mContext, mFontSelectView, new HSFontSelectViewAdapter.OnHSFontClickListener() {
            @Override
            public void onFontClick() {
                panelActionListener.showPanel(KeyboardPanel.class);
            }
        });
        mFontSelectView.setAdapter(mAdapter);
        return mFontSelectView;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(loadDataObserver);
    }


    @Override
    protected boolean onHidePanelView(int appearMode) {
        HSGlobalNotificationCenter.removeObserver(loadDataObserver);
        return super.onHidePanelView(appearMode);
    }
}

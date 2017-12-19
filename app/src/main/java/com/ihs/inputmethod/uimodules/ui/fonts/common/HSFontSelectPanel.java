package com.ihs.inputmethod.uimodules.ui.fonts.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.BaseFunctionBar;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.inputmethod.uimodules.stickerplus.PlusButton;
import com.ihs.inputmethod.uimodules.ui.customize.fragment.KeyboardFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.panelcontainer.BasePanel;
import com.ihs.panelcontainer.panel.KeyboardPanel;
import com.keyboard.common.SplashActivity;

public class HSFontSelectPanel extends BasePanel {

    public HSFontSelectPanel() {
        mContext = HSApplication.getContext();
        HSGlobalNotificationCenter.addObserver(HSSpecialCharacterManager.HS_NOTIFICATION_SPECIAL_CHARACTERS_LOAD_FINISHED, loadDataObserver);
    }


    private Context mContext;

    private HSFontSelectViewAdapter mAdapter;

    private BaseFunctionBar functionBar;


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
        functionBar = (BaseFunctionBar) panelActionListener.getBarView();
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
    public void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(loadDataObserver);
    }


    @Override
    protected boolean onHidePanelView(int appearMode) {
        functionBar.getPLusButton().setVisibility(View.GONE);
        HSGlobalNotificationCenter.removeObserver(loadDataObserver);
        return super.onHidePanelView(appearMode);
    }

    @Override
    protected boolean onShowPanelView(int appearMode) {
        functionBar.getPLusButton().setVisibility(View.VISIBLE);
        functionBar.getPLusButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Bundle bundle = new Bundle();
                bundle.putInt(ThemeHomeActivity.BUNDLE_KEY_HOME_MAIN_PAGE_TAB, ThemeHomeActivity.TAB_INDEX_KEYBOARD);
                bundle.putInt(ThemeHomeActivity.BUNDLE_KEY_HOME_INNER_PAGE_TAB, KeyboardFragment.TAB_FONT);
                HSInputMethod.hideWindow();
                ((PlusButton) v).hideNewTip();

                new Handler().postDelayed(() -> {
                    final Intent intent = new Intent();
                    intent.setClass(HSApplication.getContext(), SplashActivity.class);
                    intent.putExtras(bundle);
                    intent.putExtra(SplashActivity.JUMP_TAG,SplashActivity.JUMP_TO_THEME_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    HSApplication.getContext().startActivity(intent);
                }, 200);
            }
        });
        return super.onShowPanelView(appearMode);
    }
}

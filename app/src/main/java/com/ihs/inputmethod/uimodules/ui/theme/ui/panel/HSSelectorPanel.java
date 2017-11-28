package com.ihs.inputmethod.uimodules.ui.theme.ui.panel;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.BaseFunctionBar;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.panelcontainer.BasePanel;

/**
 * Created by yanxia on 2017/11/28.
 */

public class HSSelectorPanel extends BasePanel {
    @Override
    protected View onCreatePanelView() {
        //set functionBar setting button type
        BaseFunctionBar functionBar = (BaseFunctionBar) panelActionListener.getBarView();
        functionBar.setSettingButtonType(SettingsButton.SettingButtonType.BACK);

        @SuppressLint("InflateParams") View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.activity_main, null);
        return view;
    }
}

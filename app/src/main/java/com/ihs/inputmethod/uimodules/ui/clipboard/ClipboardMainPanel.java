package com.ihs.inputmethod.uimodules.ui.clipboard;


import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.uimodules.BaseFunctionBar;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.inputmethod.uimodules.stickerplus.PlusButton;
import com.ihs.panelcontainer.BasePanel;


public class ClipboardMainPanel extends BasePanel {
    private BaseFunctionBar functionBar;
    @Override
    protected View onCreatePanelView() {
        functionBar = (BaseFunctionBar) panelActionListener.getBarView();
        functionBar.setSettingButtonType(SettingsButton.SettingButtonType.BACK);
        return new ClipboardMainView(HSApplication.getContext());

    }
    @Override
    protected boolean onHidePanelView(int appearMode) {
        functionBar.getPLusButton().setVisibility(View.GONE);
        return super.onHidePanelView(appearMode);
    }
    @Override
    protected boolean onShowPanelView(int appearMode) {
        functionBar.getPLusButton().setVisibility(View.VISIBLE);
        functionBar.getPLusButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSInputMethod.hideWindow();
                ((PlusButton) v).hideNewTip();
            }
        });
        return super.onShowPanelView(appearMode);
    }
}
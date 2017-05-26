package com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui;

import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.view.GifPanelView;
import com.ihs.panelcontainer.BasePanel;

public final class GifPanel extends BasePanel {
    private final INotificationObserver mImeActionObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (panelView != null) {
                panelView.closeKeyboardDropDownView();
            }
        }
    };
    private GifPanelView panelView;


    public GifPanel() {
        super();
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_HIDE_WINDOW, mImeActionObserver);
    }

    @Override
    protected boolean onShowPanelView(int appearMode) {
        panelView.showPanelView();
        return super.onShowPanelView(appearMode);
    }

    @Override
    public View onCreatePanelView() {
        panelView = (GifPanelView) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.riffsy_gif_panel_view, null);
        setPanelView(panelView);
        panelView.setOnPanelActionListener(this.panelActionListener);
        return panelView;
    }

    @Override
    protected boolean onHidePanelView(int appearMode) {
        return super.onHidePanelView(appearMode);
    }

    @Override
    protected void onDestroy() {
        HSGlobalNotificationCenter.removeObserver(mImeActionObserver);
        super.onDestroy();
    }

}

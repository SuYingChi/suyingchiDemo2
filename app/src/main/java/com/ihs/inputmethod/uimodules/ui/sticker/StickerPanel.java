package com.ihs.inputmethod.uimodules.ui.sticker;

import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.panelcontainer.BasePanel;

public final class StickerPanel extends BasePanel {

    private StickerPanelView stickerPanelView;

    public StickerPanel() {
        super();
    }

    @Override
    public View onCreatePanelView() {
        stickerPanelView = (StickerPanelView) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.common_sticker_panel_view, null, false);
        setPanelView(stickerPanelView);
        return stickerPanelView;
    }

    @Override
    protected boolean onHidePanelView(int appearMode) {
        stickerPanelView.saveRecent();
        return super.onHidePanelView(appearMode);
    }

    @Override
    protected boolean onShowPanelView(int appearMode) {
        stickerPanelView.showPanelView();
        return super.onShowPanelView(appearMode);
    }

    @Override
    protected void onDestroy() {
        stickerPanelView.removeNotification();
        stickerPanelView.saveRecent();
        super.onDestroy();
    }
}

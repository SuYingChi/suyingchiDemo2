package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.panelcontainer.BasePanel;

/**
 * Created by Arthur on 17/12/8.
 */

public class RecentClipPanel extends BasePanel {
    @Override
    protected View onCreatePanelView() {
        RecentClipPanelView recentClipPanelView= (RecentClipPanelView)LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.clipboard_recent_layout, null);
        setPanelView(recentClipPanelView);
        return recentClipPanelView;
    }
}

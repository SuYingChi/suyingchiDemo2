package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.view.View;

import com.ihs.panelcontainer.BasePanel;

/**
 * Created by Arthur on 17/11/23.
 */

public class ClipboardPanel extends BasePanel {
    @Override
    protected View onCreatePanelView() {
        ClipboardMainView clipboardMainView = new ClipboardMainView();
        clipboardMainView.setKeyboardPanelActionListener(this.getPanelActionListener());
        return clipboardMainView;
    }
}

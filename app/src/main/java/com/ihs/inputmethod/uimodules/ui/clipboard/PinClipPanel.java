package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.textart.HSTextPanelView;
import com.ihs.panelcontainer.BasePanel;

/**
 * Created by Arthur on 17/12/8.
 */

public class PinClipPanel extends BasePanel {
    @Override
    protected View onCreatePanelView() {
        PinClipPanelView pinClipPanelView = (PinClipPanelView) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.clipboard_pins_layout, null, false);
        ClipPageManager.getInstance().setPinClipPanelView(pinClipPanelView);
        setPanelView(pinClipPanelView);
        return pinClipPanelView;
    }
}

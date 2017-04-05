package com.ihs.inputmethod.uimodules.ui.textart;

import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.panelcontainer.BasePanel;

public final class HSTextPanel extends BasePanel {

	private HSTextPanelView textPanelView;

	public HSTextPanel() {
		super();
	}

	@Override
	protected View onCreatePanelView() {
		textPanelView = (HSTextPanelView) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.common_text_panel_view, null,false);
		setPanelView(textPanelView);
		return textPanelView;
	}

	@Override
	protected boolean onHidePanelView(int appearMode) {
		textPanelView.saveRecent();
		return super.onHidePanelView(appearMode);
	}

	@Override
	protected boolean onShowPanelView(int appearMode) {
		textPanelView.showPanelView();
		return super.onShowPanelView(appearMode);
	}

	@Override
	protected void onDestroy() {
		textPanelView.saveRecent();
		super.onDestroy();
	}

}

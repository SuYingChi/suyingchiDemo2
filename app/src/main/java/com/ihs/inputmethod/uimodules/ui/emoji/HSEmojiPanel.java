package com.ihs.inputmethod.uimodules.ui.emoji;

import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.panelcontainer.BasePanel;

public final class HSEmojiPanel extends BasePanel {

	private HSEmojiPanelView emojiPanelView;

	public HSEmojiPanel() {
		super();
	}

	@Override
	protected View onCreatePanelView() {
		emojiPanelView = (HSEmojiPanelView) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.common_emoji_panel_view, null);
		setPanelView(emojiPanelView);
		return emojiPanelView;
	}

	@Override
	protected boolean onHidePanelView(int appearMode) {
		emojiPanelView.saveRecent();
		return super.onHidePanelView(appearMode);
	}

	@Override
	protected boolean onShowPanelView(int appearMode) {
		emojiPanelView.showPanelView();
		return super.onShowPanelView(appearMode);
	}

	@Override
	protected void onDestroy() {
		emojiPanelView.saveRecent();
		super.onDestroy();
	}

}

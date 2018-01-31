package com.ihs.inputmethod.uimodules.ui.emoticon;

import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.panelcontainer.BasePanel;

/**
 * Created by wenbinduan on 2016/11/21.
 */

public final class HSEmoticonPanel extends BasePanel {

	private HSEmoticonView emoticonView;

	public HSEmoticonPanel() {
	}

	@Override
	public View onCreatePanelView() {
		if(emoticonView==null){
			emoticonView=new HSEmoticonView();
			emoticonView.setKeyboardPanelActionListener(this.panelActionListener);
//			emoticonView.setKeyboardPanel(FakeKeyboardPanel.class,this.panelActionListener.getKeyboardView());
		}
		return emoticonView;
	}

	@Override
	protected boolean onShowPanelView(int appearMode) {
		emoticonView.showLastPanel();
		return super.onShowPanelView(appearMode);
	}

	@Override
	public boolean onHidePanelView(int appearMode) {
		setPanelView(null);
		return super.onHidePanelView(appearMode);
	}


	@Override
	public View getPanelView() {
		return emoticonView;
	}

	@Override
	protected void onDestroy() {
		emoticonView.onDestroy();
	}
}

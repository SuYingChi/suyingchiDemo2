package com.ihs.inputmethod.uimodules.ui.emoticon;

import android.view.View;

import com.ihs.inputmethod.framework.InputView;
import com.ihs.inputmethod.suggestions.SuggestionStripView;
import com.ihs.panelcontainer.BasePanel;

/**
 * Created by wenbinduan on 2016/12/2.
 */

public final class FakeKeyboardPanel extends BasePanel {

	public FakeKeyboardPanel(){
		super();
	}
	
	@Override
	protected View onCreatePanelView() {
		return null;
	}

	@Override
	public void setPanelView(View rootView) {
		super.setPanelView(rootView);
		InputView inputView = (InputView) getPanelView();
		SuggestionStripView suggestionStripView = inputView.getSuggestionStripView();
		suggestionStripView.setOnStripViewStateChangeListener(null);
	}
}

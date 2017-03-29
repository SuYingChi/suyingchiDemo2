package com.ihs.inputmethod.uimodules.ui.emoticon;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.panelcontainer.BasePanel;
import com.ihs.panelcontainer.KeyboardPanelSwitchContainer;

/**
 * Created by wenbinduan on 2016/11/21.
 */

public final class HSEmoticonView extends KeyboardPanelSwitchContainer {

	final HSEmoticonActionBar actionBar;

	public HSEmoticonView() {
		super();
		this.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
		setBarPosition(BAR_BOTTOM);
		actionBar= (HSEmoticonActionBar) View.inflate(HSApplication.getContext(),R.layout.emoticon_action_bar,null);
		setBarView(actionBar);
		actionBar.setContainerListener(this);
		final Resources res = getContext().getResources();
		final int height = HSResourceUtils.getDefaultKeyboardHeight(res) +res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height);
		setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
	}


	@Override
	public void showPanel(Class panelClass) {
		super.showPanel(panelClass);
		actionBar.selectPanelBtn(panelClass);
	}

	public void setKeyboardPanelActionListener(BasePanel.OnPanelActionListener panelActionListener) {
		actionBar.setKeyboardPanelActionListener(panelActionListener);
	}

	public void showLastPanel() {
		SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(getContext());
		String lastPanel=sp.getString("emoticon_last_show_panel_name","emoji");
		Class<?> clazz=actionBar.getPanelClass(lastPanel);
		showPanel(clazz);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		actionBar.release();
	}
}

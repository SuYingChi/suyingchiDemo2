package com.ihs.inputmethod.uimodules.ui.common.adapter;

import android.graphics.drawable.Drawable;

import com.ihs.inputmethod.uimodules.ui.common.BaseTabViewAdapter;

import java.util.List;

/**
 * Created by wenbinduan on 2016/11/22.
 */

public final class HSEmojiTabAdapter extends BaseTabViewAdapter {

	public HSEmojiTabAdapter(List<String> tabs,  OnTabChangeListener listener) {
		super(tabs,  listener);
	}

	@Override
	protected Drawable getTabView(String tab) {
		return getBtnDrawable("tabbar_"+tab);
	}

}

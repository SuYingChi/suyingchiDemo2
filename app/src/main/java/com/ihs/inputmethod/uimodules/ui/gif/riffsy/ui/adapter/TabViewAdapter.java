package com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.adapter;

import android.graphics.drawable.Drawable;

import com.ihs.inputmethod.uimodules.ui.common.BaseTabViewAdapter;

import java.util.List;

/**
 * Created by dsapphire on 16/1/11.
 */
public final class TabViewAdapter extends BaseTabViewAdapter {

	public TabViewAdapter(List<String> tabs, OnTabChangeListener listener){
		super(tabs,listener);
	}

	@Override
	protected Drawable getTabView(String tab) {
		return getBtnDrawable("tabbar_"+tab);
	}

}

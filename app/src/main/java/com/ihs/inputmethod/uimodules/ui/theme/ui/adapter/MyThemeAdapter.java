package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter;

import android.app.Activity;

import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.CustomizedTitleAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.ThemeTitleAdapterDelegate;

/**
 * Created by wenbinduan on 2016/12/23.
 */

public final class MyThemeAdapter extends CommonThemeCardAdapter {

	public MyThemeAdapter(Activity activity, ThemeCardItemClickListener themeCardItemClickListener) {
		super(activity, themeCardItemClickListener, false);
		delegatesManager.addDelegate(new ThemeTitleAdapterDelegate())
				.addDelegate(new CustomizedTitleAdapterDelegate());
	}

}

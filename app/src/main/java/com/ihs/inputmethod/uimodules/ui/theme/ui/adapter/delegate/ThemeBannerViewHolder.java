package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class ThemeBannerViewHolder extends RecyclerView.ViewHolder {

	ViewPager viewPager;
	public ThemeBannerViewHolder(View itemView) {
		super(itemView);
		viewPager= (ViewPager) itemView;
	}
}

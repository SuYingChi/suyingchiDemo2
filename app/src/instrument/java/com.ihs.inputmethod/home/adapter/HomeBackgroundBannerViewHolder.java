package com.ihs.inputmethod.home.adapter;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.widget.HomeHotBackgroundIndicatorView;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class HomeBackgroundBannerViewHolder extends RecyclerView.ViewHolder {

	ViewPager viewPager;
	HomeHotBackgroundIndicatorView indicatorView;
	public HomeBackgroundBannerViewHolder(View itemView) {
		super(itemView);
		viewPager= itemView.findViewById(R.id.home_hot_background_banner_viewpager);
		indicatorView = itemView.findViewById(R.id.indicator);
	}
}

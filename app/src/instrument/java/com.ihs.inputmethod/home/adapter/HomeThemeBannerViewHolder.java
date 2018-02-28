package com.ihs.inputmethod.home.adapter;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public final class HomeThemeBannerViewHolder extends RecyclerView.ViewHolder {

	ViewPager viewPager;
	public HomeThemeBannerViewHolder(View itemView) {
		super(itemView);
		viewPager= (ViewPager) itemView;
	}
}

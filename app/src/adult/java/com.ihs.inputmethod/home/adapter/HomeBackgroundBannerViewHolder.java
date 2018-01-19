package com.ihs.inputmethod.home.adapter;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class HomeBackgroundBannerViewHolder extends RecyclerView.ViewHolder {

	ViewPager viewPager;
	public HomeBackgroundBannerViewHolder(View itemView) {
		super(itemView);
		viewPager= (ViewPager) itemView;
	}
}

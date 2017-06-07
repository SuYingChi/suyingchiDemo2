package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeBannerAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;

import java.util.List;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class ThemeBannerAdapterDelegate extends AdapterDelegate<List<ThemeHomeModel>> {

	private Activity activity;
	private boolean isThemeAnalyticsEnabled;

	public ThemeBannerAdapterDelegate(Activity activity, boolean isThemeAnalyticsEnabled) {
		this.activity = activity;
		this.isThemeAnalyticsEnabled = isThemeAnalyticsEnabled;
	}

	@Override
	protected boolean isForViewType(@NonNull List<ThemeHomeModel> items, int position) {
		return items.get(position).isBanner;
	}

	@NonNull
	@Override
	protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
		ThemeBannerViewHolder viewHolder=new ThemeBannerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_banners,parent,false));

		int bannerWidth = HSApplication.getContext().getResources().getDisplayMetrics().widthPixels
				- HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.theme_store_viewpager_page_margin) * 2
				- HSApplication.getContext().getResources().getDimensionPixelOffset(R.dimen.theme_store_viewpager_padding_left)
				- HSApplication.getContext().getResources().getDimensionPixelOffset(R.dimen.theme_store_viewpager_padding_right);
		int bannerHeight = (int) (bannerWidth * (10 / 19f));

		ThemeBannerAdapter themeBannerAdapter = new ThemeBannerAdapter(activity,bannerWidth,bannerHeight);
		themeBannerAdapter.setThemeAnalyticsEnabled(isThemeAnalyticsEnabled);
		ViewPager viewPager =viewHolder.viewPager;
		viewPager.setPageMargin(HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.theme_store_viewpager_page_margin));
		viewPager.getLayoutParams().height = (int) (bannerHeight + HSApplication.getContext().getResources().getDisplayMetrics().density * 10);
		themeBannerAdapter.setViewPager(viewPager);
		themeBannerAdapter.initData();
		viewPager.setAdapter(themeBannerAdapter);
		// themeBannerAdapter.startToFetchNativeAd();
		themeBannerAdapter.startAutoScroll();

		return viewHolder;
	}

	@Override
	protected void onBindViewHolder(@NonNull List<ThemeHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {

//		((ThemeBannerViewHolder)holder).viewPager.getAdapter().notifyDataSetChanged();
//		((ThemeBannerAdapter)((ThemeBannerViewHolder)holder).viewPager.getAdapter()).startAutoScroll();
	}


	@Override
	protected void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
		super.onViewDetachedFromWindow(holder);
		ThemeBannerViewHolder viewHolder= (ThemeBannerViewHolder) holder;
		((ThemeBannerAdapter)viewHolder.viewPager.getAdapter()).stopAutoScroll();
	}

	@Override
	protected void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
		super.onViewAttachedToWindow(holder);
		ThemeBannerViewHolder viewHolder= (ThemeBannerViewHolder) holder;
		((ThemeBannerAdapter)viewHolder.viewPager.getAdapter()).startAutoScroll();
	}

	@Override
	public int getSpanSize(List<ThemeHomeModel> items, int position) {
		return 2;
	}
}

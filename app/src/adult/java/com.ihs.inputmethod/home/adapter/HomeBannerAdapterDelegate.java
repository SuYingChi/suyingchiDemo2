package com.ihs.inputmethod.home.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.home.HomeModel.HomeModel;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeBannerAdapter;

import java.util.List;

public final class HomeBannerAdapterDelegate extends AdapterDelegate<List<HomeModel>> {

    private Activity activity;
    private boolean isThemeAnalyticsEnabled;

    public HomeBannerAdapterDelegate(Activity activity, boolean isThemeAnalyticsEnabled) {
        this.activity = activity;
        this.isThemeAnalyticsEnabled = isThemeAnalyticsEnabled;
    }

    @Override
    protected boolean isForViewType(@NonNull List<HomeModel> items, int position) {
        return items.get(position).isBanner;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        HomeBannerViewHolder viewHolder = new HomeBannerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_hot_background_banner, parent, false));

        int bannerWidth = HSApplication.getContext().getResources().getDisplayMetrics().widthPixels;
        int bannerHeight = (int) (bannerWidth * (10 / 19f));

        ThemeBannerAdapter themeBannerAdapter = new ThemeBannerAdapter(activity, bannerWidth, bannerHeight);
        themeBannerAdapter.setThemeAnalyticsEnabled(isThemeAnalyticsEnabled);
        ViewPager viewPager = viewHolder.viewPager;
        viewPager.getLayoutParams().height = (int) (bannerHeight + HSApplication.getContext().getResources().getDisplayMetrics().density * 10);
        themeBannerAdapter.setViewPager(viewPager);
        themeBannerAdapter.initData();
        viewPager.setAdapter(themeBannerAdapter);
        themeBannerAdapter.startAutoScroll();

        return viewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<HomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {

    }


    @Override
    protected void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        HomeBannerViewHolder viewHolder = (HomeBannerViewHolder) holder;
        ((ThemeBannerAdapter) viewHolder.viewPager.getAdapter()).stopAutoScroll();
    }

    @Override
    protected void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        HomeBannerViewHolder viewHolder = (HomeBannerViewHolder) holder;
        ((ThemeBannerAdapter) viewHolder.viewPager.getAdapter()).startAutoScroll();
    }

    @Override
    public int getSpanSize(List<HomeModel> items, int position) {
        return 2;
    }
}

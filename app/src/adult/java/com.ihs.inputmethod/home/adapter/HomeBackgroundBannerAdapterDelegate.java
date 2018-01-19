package com.ihs.inputmethod.home.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ihs.inputmethod.home.HomeModel.HomeModel;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;

import java.util.List;

public final class HomeBackgroundBannerAdapterDelegate extends AdapterDelegate<List<HomeModel>> {

    private Activity activity;
    private boolean isThemeAnalyticsEnabled;

    public HomeBackgroundBannerAdapterDelegate(Activity activity, boolean isThemeAnalyticsEnabled) {
        this.activity = activity;
        this.isThemeAnalyticsEnabled = isThemeAnalyticsEnabled;
    }

    @Override
    protected boolean isForViewType(@NonNull List<HomeModel> items, int position) {
        return items.get(position).isBackgroundBanner;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        HomeBackgroundBannerViewHolder viewHolder = new HomeBackgroundBannerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_hot_background_banner, parent, false));

        int bannerWidth = parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
        int bannerHeight = (int) (bannerWidth * (135 / 338f));

        HomeBackgroundBannerAdapter homeBannerAdapter = new HomeBackgroundBannerAdapter(activity, bannerWidth, bannerHeight);
        homeBannerAdapter.setThemeAnalyticsEnabled(isThemeAnalyticsEnabled);
        ViewPager viewPager = viewHolder.viewPager;
        viewPager.getLayoutParams().height = bannerHeight;
        homeBannerAdapter.setViewPager(viewPager);
        homeBannerAdapter.initData();
        viewPager.setAdapter(homeBannerAdapter);
        homeBannerAdapter.startAutoScroll();

        return viewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<HomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {

    }


    @Override
    protected void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        HomeBackgroundBannerViewHolder viewHolder = (HomeBackgroundBannerViewHolder) holder;
        ((HomeBackgroundBannerAdapter) viewHolder.viewPager.getAdapter()).stopAutoScroll();
    }

    @Override
    protected void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        HomeBackgroundBannerViewHolder viewHolder = (HomeBackgroundBannerViewHolder) holder;
        ((HomeBackgroundBannerAdapter) viewHolder.viewPager.getAdapter()).startAutoScroll();
    }

    @Override
    public int getSpanSize(List<HomeModel> items, int position) {
        return 2;
    }
}

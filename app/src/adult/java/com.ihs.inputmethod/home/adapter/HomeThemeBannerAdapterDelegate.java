package com.ihs.inputmethod.home.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.home.HomeModel.HomeModel;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.utils.DisplayUtils;

import java.util.List;


public final class HomeThemeBannerAdapterDelegate extends AdapterDelegate<List<HomeModel>> {
    private Activity activity;
    private boolean isThemeAnalyticsEnabled;

    public HomeThemeBannerAdapterDelegate(Activity activity, boolean isThemeAnalyticsEnabled) {
        this.activity = activity;
        this.isThemeAnalyticsEnabled = isThemeAnalyticsEnabled;
    }

    @Override
    protected boolean isForViewType(@NonNull List<HomeModel> items, int position) {
        return items.get(position).isThemeBanner;
    }

    @Override
    public int getSpanSize(List<HomeModel> items, int position) {
        return 2;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        ViewPager viewPager = new ViewPager(parent.getContext());
        RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(viewPager) {
        };

        int bannerWidth = parent.getMeasuredWidth();
        int bannerHeight = (int) (bannerWidth * (150 / 317f));

        HomeThemeBannerAdapter adapter = new HomeThemeBannerAdapter(activity, bannerWidth, bannerHeight);
        adapter.setThemeAnalyticsEnabled(isThemeAnalyticsEnabled);
        viewPager.setLayoutParams(new RelativeLayout.LayoutParams(bannerWidth, (int) (bannerHeight + HSApplication.getContext().getResources().getDisplayMetrics().density * 10)));
        adapter.setViewPager(viewPager);
        adapter.initData();
        viewPager.setAdapter(adapter);
        adapter.startAutoScroll();
        viewHolder.itemView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dip2px(parent.getContext(), 150)));
        return viewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<HomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {

    }
}

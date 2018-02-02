package com.ihs.inputmethod.home.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;

import java.util.List;

public final class HomeBackgroundBannerAdapterDelegate extends AdapterDelegate<List<HomeModel>> {
    private HomeBackgroundBannerAdapter.OnBackgroundBannerClickListener onBackgroundBannerClickListener;
    private HomeBackgroundBannerAdapter homeBannerAdapter;

    public HomeBackgroundBannerAdapterDelegate(HomeBackgroundBannerAdapter.OnBackgroundBannerClickListener onBackgroundBannerClickListener) {
        this.onBackgroundBannerClickListener = onBackgroundBannerClickListener;
    }

    @Override
    protected boolean isForViewType(@NonNull List<HomeModel> items, int position) {
        return items.get(position).isBackgroundBanner;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        HomeBackgroundBannerViewHolder viewHolder = new HomeBackgroundBannerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_hot_background_banner, parent, false));

        ViewPager viewPager = viewHolder.viewPager;
        viewPager.setPageMargin(HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.theme_store_viewpager_page_margin));

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) viewHolder.itemView.getLayoutParams();
        int width = parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight() - layoutParams.leftMargin - layoutParams.rightMargin;
        int height = (int) (width * (135 / 338f));
        layoutParams.height = height;
        layoutParams.width = width;
        layoutParams.bottomMargin = HSDisplayUtils.dip2px(parent.getContext(), 20);

        homeBannerAdapter = new HomeBackgroundBannerAdapter(onBackgroundBannerClickListener);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewHolder.indicatorView.updateIndicator(position % homeBannerAdapter.getRealCount(), homeBannerAdapter.getRealCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        homeBannerAdapter.setViewPager(viewPager);
        homeBannerAdapter.initData();
        viewPager.setAdapter(homeBannerAdapter);
        return viewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<HomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {

    }


    @Override
    protected void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (homeBannerAdapter != null) {
            homeBannerAdapter.stopAutoScroll();
        }
    }

    @Override
    protected void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (homeBannerAdapter != null) {
            homeBannerAdapter.startAutoScroll();
        }
    }

    @Override
    public int getSpanSize(List<HomeModel> items, int position) {
        return 2;
    }
}

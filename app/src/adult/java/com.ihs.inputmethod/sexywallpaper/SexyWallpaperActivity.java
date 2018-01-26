package com.ihs.inputmethod.sexywallpaper;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.feature.common.ViewUtils;
import com.ihs.inputmethod.feature.common.LauncherConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.view.CategoryItem;
import com.ihs.inputmethod.uimodules.ui.customize.view.OnlineWallpaperListView;
import com.ihs.inputmethod.widget.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jixiang on 18/1/25.
 */

public class SexyWallpaperActivity extends HSAppCompatActivity {
    private static final int EXTRA_TABS_COUNT = 0;

    private WallpaperPagerAdapter mAdapter;
    private SlidingTabLayout slidingTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sexy_wallpaper);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.activity_sexy_wallpaper_title);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setup(0);
    }

    public void setup(int index) {
        slidingTabLayout = ViewUtils.findViewById(this, R.id.tab_layout);

        final ViewPager viewPage = ViewUtils.findViewById(this, R.id.wallpaper_pager);
        mAdapter = new WallpaperPagerAdapter();
        viewPage.setAdapter(mAdapter);
        slidingTabLayout.setViewPager(viewPage);

        int indexAbsolute = index;
        viewPage.setCurrentItem(indexAbsolute, false);

        List<CategoryItem> data = new ArrayList<>();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            CategoryItem item = new CategoryItem(mAdapter.getPageTitle(i).toString(), i == indexAbsolute);
            data.add(item);
        }
    }

    private class WallpaperPagerAdapter extends PagerAdapter {
        private final List<Map<String, ?>> mCategoryConfigs;

        private List<OnlineWallpaperListView> mCategoryTabContents = new ArrayList<>(22);

        @SuppressWarnings("unchecked")
        WallpaperPagerAdapter() {
            mCategoryConfigs = (List<Map<String, ?>>) HSConfig.getList("Application", "Wallpapers");
        }

        public void onDestroy() {

            for (int i = 0; i < mCategoryTabContents.size(); i++) {
                OnlineWallpaperListView list = mCategoryTabContents.get(i);
                if (list != null && list.adapter != null) {
                    list.adapter.onDestroy();
                }
            }
        }

        @Override
        public int getCount() {
            return mCategoryConfigs.size() + EXTRA_TABS_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int positionAbsolute) {
            int position = positionAbsolute;
            int categoryIndex = position - EXTRA_TABS_COUNT;
            return LauncherConfig.getMultilingualString(mCategoryConfigs.get(categoryIndex), "CategoryName");
        }

        @Override
        public Object instantiateItem(ViewGroup container, int positionAbsolute) {
            int position = positionAbsolute;
            View initView;
            int categoryIndex = position - EXTRA_TABS_COUNT;
            for (int i = mCategoryTabContents.size(); i <= categoryIndex; i++) {
                createSingleCategoryTabContent(i);
            }
            OnlineWallpaperListView list = mCategoryTabContents.get(categoryIndex);
            list.startLoading();
            initView = list;
            container.addView(initView);
            return initView;
        }

        @SuppressLint("InflateParams")
        private void createSingleCategoryTabContent(final int categoryIndex) {
            OnlineWallpaperListView categoryListView = (OnlineWallpaperListView) View.inflate(SexyWallpaperActivity.this, R.layout.wallpaper_list_page, null);

            String categoryName = "";
            int position = categoryIndex + EXTRA_TABS_COUNT;
            int positionAbsolute = position;
            CharSequence categoryNameCs = getPageTitle(positionAbsolute);
            if (null != categoryNameCs) {
                categoryName = categoryNameCs.toString();
            }
            categoryListView.setCategoryName(categoryName);
            categoryListView.setCategoryIndex(categoryIndex);
            mCategoryTabContents.add(categoryIndex, categoryListView);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

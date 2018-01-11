package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.config.HSConfig;
import com.ihs.feature.common.PreferenceHelper;
import com.ihs.feature.common.ViewUtils;
import com.ihs.inputmethod.feature.common.LauncherConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.adapter.CategoryViewAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.keyboardutils.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by guonan.lv on 17/9/4.
 */

public class OnlineWallpaperPage extends RelativeLayout {

    private static final int EXTRA_TABS_COUNT = 0;

    // --Commented out by Inspection (18/1/11 下午2:41):private static final int TAB_INDEX_HOT = 0;
    private static final int TAB_INDEX_3D = 1;

    public static final String PREF_KEY_3D_WALLPAPER_VIEW_SHOWN = "3d_wallpaper_view_shown";

    private WallpaperPagerAdapter mAdapter;

    private OnlineWallpaperTabLayout mTabs;
    private GridView mGridView;
    private TextView mCategoriesTitle;
    private List<Integer> mScrollStates = new ArrayList<>();
    // --Commented out by Inspection (18/1/11 下午2:41):private boolean mIsTabNoClickSelected;
    // --Commented out by Inspection (18/1/11 下午2:41):private AnimatorSet mAnimatorSet;
    float sumPositionAndPositionOffset;

    public OnlineWallpaperPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setup(0);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void onDestroy() {
//
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public void setup(int index) {
        mTabs = ViewUtils.findViewById(this, R.id.wallpaper_tabs);

        final ViewPager viewPage = ViewUtils.findViewById(this, R.id.wallpaper_pager);
        mAdapter = new WallpaperPagerAdapter(getContext());
        viewPage.setAdapter(mAdapter);
        mTabs.setupWithViewPager(viewPage);
//        Utils.configTabLayoutText(mTabs, FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD), 14f);

        mTabs.setOnScrollListener(new OnlineWallpaperTabLayout.OnScrollListener() {
            @Override
            public void onScrollFinished(boolean isScrollLeft, boolean isScrollRight) {
//                LauncherAnalytics.logEvent("Wallpaper_TopTab_Slided");
            }
        });

        mTabs.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                String title = (String) mTabs.getTabAt(position).getText();
                HSAnalytics.logEvent("app_tab_top_wallpaper_clicked", "tabName", title.toLowerCase());
            }
        });

        int indexAbsolute = index;
        viewPage.setCurrentItem(indexAbsolute, false);

        viewPage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position + positionOffset > sumPositionAndPositionOffset) {
                } else if (position != 0 || positionOffset != 0 || sumPositionAndPositionOffset != 0 || positionOffsetPixels != 0) {

                }
                sumPositionAndPositionOffset = position + positionOffset;
            }

            @Override
            public void onPageSelected(final int positionAbsolute) {
                resetCategoryGrids();
                ((CategoryItem) mGridView.getAdapter().getItem(positionAbsolute)).setSelected(true);
                ((CategoryViewAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                ((ThemeHomeActivity) getContext()).getLayoutWrapper().show();
                int position = positionAbsolute;
                if (position == TAB_INDEX_3D) {
                    PreferenceHelper.getDefault().putBoolean(PREF_KEY_3D_WALLPAPER_VIEW_SHOWN, true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mScrollStates.add(state);
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    for (Integer stateItem : mScrollStates) {
                        if (stateItem == ViewPager.SCROLL_STATE_DRAGGING) {
                            break;
                        }
                    }
                    mScrollStates.clear();
                }

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewUtils.findViewById(this, R.id.tab_layout_container)
                    .setElevation(CommonUtils.pxFromDp(1));
        }

        mGridView = ViewUtils.findViewById(this, R.id.categories_grid_view);

        List<CategoryItem> data = new ArrayList<>();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            CategoryItem item = new CategoryItem(mAdapter.getPageTitle(i).toString(), i == indexAbsolute);
            data.add(item);
        }

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                ((CategoryViewAdapter) mGridView.getAdapter()).setTextAnimationEnabled(false);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        mCategoriesTitle = ViewUtils.findViewById(this, R.id.categories_title);

        final CategoryViewAdapter categoryViewAdapter = new CategoryViewAdapter(getContext(), data);
        mGridView.setAdapter(categoryViewAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= mTabs.getTabCount() || position == mTabs.getSelectedTabPosition()) {
                    return;
                }


                ((CategoryViewAdapter) parent.getAdapter()).setTextAnimationEnabled(false);
                ((CategoryItem) parent.getAdapter().getItem(mTabs.getSelectedTabPosition())).setSelected(false);
                ((CategoryItem) parent.getAdapter().getItem(position)).setSelected(true);
                ((CategoryViewAdapter) parent.getAdapter()).notifyDataSetChanged();

                viewPage.setCurrentItem(position, true);
            }
        });
    }

    private void resetCategoryGrids() {
        for (int i = 0; i < mTabs.getTabCount(); i++) {
            ((CategoryItem) mGridView.getAdapter().getItem(i)).setSelected(false);
        }
    }

    public boolean isShowingCategories() {
        return mCategoriesTitle.getVisibility() != GONE;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void hideCategoriesView() {
//        if (isShowingCategories()) {
////            arrowClicked(mGridView, mCategoriesTitle, mArrowLeftPart, mArrowRightPart, "Navigation bar_Back");
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }

    private class WallpaperPagerAdapter extends PagerAdapter {
        private final List<Map<String, ?>> mCategoryConfigs;

        // --Commented out by Inspection (18/1/11 下午2:41):private Context mContext;

// --Commented out by Inspection START (18/1/11 下午2:41):
//        // Views
//        private OnlineWallpaperListView mHotTabContent;
// --Commented out by Inspection STOP (18/1/11 下午2:41)
        private List<OnlineWallpaperListView> mCategoryTabContents = new ArrayList<>(22);

        @SuppressWarnings("unchecked")
        WallpaperPagerAdapter(Context context) {
            mCategoryConfigs = (List<Map<String, ?>>) HSConfig.getList("Application", "Wallpapers");
        }

// --Commented out by Inspection START (18/1/11 下午2:41):
//        public void onDestroy() {
//
//            for (int i = 0; i < mCategoryTabContents.size(); i++) {
//                OnlineWallpaperListView list = mCategoryTabContents.get(i);
//                if (list != null && list.adapter != null) {
//                    list.adapter.onDestroy();
//                }
//            }
//        }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

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
            OnlineWallpaperListView categoryListView = (OnlineWallpaperListView) LayoutInflater.from(getContext())
                    .inflate(R.layout.wallpaper_list_page, OnlineWallpaperPage.this, false);

            String categoryName = "";
            int position = categoryIndex + EXTRA_TABS_COUNT;
            int positionAbsolute = position;
            CharSequence categoryNameCs = getPageTitle(positionAbsolute);
            if (null != categoryNameCs) {
                categoryName = categoryNameCs.toString();
            }
            categoryListView.setCategoryName(categoryName);
            categoryListView.setCategoryIndex(categoryIndex);
//            categoryListView.setScenario(WallpaperMgr.Scenario.ONLINE_CATEGORY);
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

}

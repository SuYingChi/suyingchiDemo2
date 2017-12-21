package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiAnimationView;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiCategory;

import java.util.List;

class FacemojiPalettesAdapter extends PagerAdapter {
    private static String TAG = "FacemojiPalettesAdapter";
    private static final int PAGER_TOP_PADDING_PX = 0;
    private static final int PAGER_BOTTOM_PADDING_PX = 0;
    private static final int GRID_VERTICAL_GAP_PX = 20;
    private static final int GRID_COLUMN_NUMBER = 2;
    private static final int GRID_ROW_NUMBER = 3;

    private int pagerHeight;
    private int mActivePosition = 0;
    private List<FacemojiCategory> categories;
    private SparseArray<GridView> mActivePageViews = new SparseArray<>();

    private Activity activity;
    private MyFacemojiActivity.PagerCallback callback;

    public FacemojiPalettesAdapter(Activity activity, List<FacemojiCategory> categories, int pagerHeight, MyFacemojiActivity.PagerCallback callback) {
        this.categories = categories;
        this.activity = activity;
        this.callback = callback;
        this.pagerHeight = pagerHeight;
    }


    public void setData(List<FacemojiCategory> categories) {
        this.categories = categories;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position, final Object object) {
        if (mActivePosition == position) {
            return;
        }
        mActivePosition = position;
    }

    public void clear() {
        mActivePageViews.clear();
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        GridView view = mActivePageViews.get(position);
        if (view != null) {
            if (view.getParent() != null){
                ((ViewGroup)view.getParent()).removeView(view);
            }
            container.addView(view);
            return view;
        }
        mActivePageViews.remove(position);
        final GridViewWithHeaderAndFooter stickerPageGridView = new GridViewWithHeaderAndFooter(HSApplication.getContext());
        stickerPageGridView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setGridViewLayoutProperties(stickerPageGridView);
        FacemojiGridAdapter adapter = new FacemojiGridAdapter(activity,categories.get(position).getStickerList());
        stickerPageGridView.setAdapter(adapter);
        container.addView(stickerPageGridView);
        mActivePageViews.put(position, stickerPageGridView);
        return stickerPageGridView;
    }

    public void notifyDownloaded(int position, int currentPagerPosition) {
        GridViewWithHeaderAndFooter gridView = (GridViewWithHeaderAndFooter)mActivePageViews.get(position);
        if (gridView != null) {
            FacemojiGridAdapter adapter = (FacemojiGridAdapter) gridView.getOriginalAdapter();
            if (adapter != null){
                if (position == currentPagerPosition) {
                    adapter.setAllowPlayAnim(true);
                }
                adapter.setFacemojiStickerList(categories.get(position).getStickerList());
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void startAnim(int position){
        GridViewWithHeaderAndFooter gridView = (GridViewWithHeaderAndFooter)mActivePageViews.get(position);
        if (gridView != null) {
            FacemojiGridAdapter adapter = (FacemojiGridAdapter) gridView.getOriginalAdapter();
            if (adapter != null){
                adapter.setAllowPlayAnim(true);
            }
            for (int i = 0; i < gridView.getChildCount(); ++i) {
                View view = gridView.getChildAt(i);
                if (view instanceof AnimationLayout) {
                    ((FacemojiAnimationView) ((AnimationLayout) view).getChildAt(0)).startAnim();
                }
            }
        }
    }

    public void stopAllAnimations() {
        for (int i = 0; i < mActivePageViews.size(); ++i) {
            stopAnim(i);
        }
    }

    public void stopAnim(int posotion){
        GridViewWithHeaderAndFooter gridView = (GridViewWithHeaderAndFooter) mActivePageViews.get(posotion);
        if (gridView != null) {
            FacemojiGridAdapter adapter = (FacemojiGridAdapter) gridView.getOriginalAdapter();
            if (adapter != null){
                adapter.setAllowPlayAnim(false);
            }
            for (int i = 0; i < gridView.getChildCount(); ++i) {
                View view =  gridView.getChildAt(i);
                if (view instanceof AnimationLayout){
                    ((FacemojiAnimationView)((AnimationLayout) view).getChildAt(0)).stopAnim();
                }
            }
        }
    }

    private void setGridViewLayoutProperties(GridViewWithHeaderAndFooter stickerPageGridView) {
        int gap = HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.facemoji_myfacemoji_grid_gap);
        stickerPageGridView.setGravity(Gravity.CENTER);
        stickerPageGridView.setNumColumns(GRID_COLUMN_NUMBER);
        stickerPageGridView.setVerticalSpacing(HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.facemoji_grid_item_vertical_space));
        stickerPageGridView.setHorizontalSpacing(HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.facemoji_grid_item_horizontal_space));
        stickerPageGridView.setVerticalScrollBarEnabled(false);

        int dimension = (int) ((float) (pagerHeight - gap * (GRID_ROW_NUMBER - 1)) / GRID_ROW_NUMBER);
        int topPadding = (pagerHeight - dimension * GRID_ROW_NUMBER - gap * (GRID_ROW_NUMBER - 1)) / 2 - 5;
        ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(topPadding, 0);
        View headerView = new View(HSApplication.getContext());
        headerView.setLayoutParams(param);

        View footerView = new View(HSApplication.getContext());
        footerView.setLayoutParams(param);
        stickerPageGridView.addHeaderView(headerView);
        stickerPageGridView.addFooterView(footerView);
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        if (isCurrentPage(position)) {
            Log.d(TAG, "destroyItem: " + position + " return");
            return;
        }
        container.removeView(mActivePageViews.get(position));
    }

    private boolean isCurrentPage(final int position) {
        return position == callback.getCurrentPagerPosition();
    }


    public void finish(){
        GridViewWithHeaderAndFooter currentPageView = (GridViewWithHeaderAndFooter)mActivePageViews.get(mActivePosition);
        if (currentPageView!=null){
            ((FacemojiGridAdapter)currentPageView.getOriginalAdapter()).finish();
        }
    }

}
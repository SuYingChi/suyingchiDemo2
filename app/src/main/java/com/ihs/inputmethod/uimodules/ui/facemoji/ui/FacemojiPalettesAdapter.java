package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;

class FacemojiPalettesAdapter extends PagerAdapter {
    private static String TAG = "FacemojiPalettesAdapter";
    private static final int PAGER_TOP_PADDING_PX = 0;
    private static final int PAGER_BOTTOM_PADDING_PX = 0;
    private static final int GRID_VERTICAL_GAP_PX = 20;
    private static final int GRID_COLUMN_NUMBER = 2;
    private static final int GRID_ROW_NUMBER = 3;

    private FacemojiManager.FacemojiType facemojiType = FacemojiManager.FacemojiType.CLASSIC;


    private SparseArray<GridView> mActivePageViews = new SparseArray<>();
    private int stickerDimension;
    private int pagerHeight;

    private int mActivePosition = 0;
    private MyFacemojiActivity.PagerCallback callback;

    public FacemojiPalettesAdapter(int pagerHeight, MyFacemojiActivity.PagerCallback callback) {
        this.callback = callback;
        this.pagerHeight = pagerHeight;
    }

    public void setFacemojiType(FacemojiManager.FacemojiType facemojiType) {
        this.facemojiType = facemojiType;
    }

    @Override
    public int getCount() {
        return FacemojiManager.getInstance().getCategories(facemojiType).size();
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
            container.addView(view);
            return view;
        }
        mActivePageViews.remove(position);
        final GridViewWithHeaderAndFooter stickerPageGridView = new GridViewWithHeaderAndFooter(HSApplication.getContext());
        setGridViewLayoutProperties(stickerPageGridView);
        FacemojiGridAdapter adapter = new FacemojiGridAdapter(FacemojiManager.getInstance().getStickerList(facemojiType,position), stickerDimension);
        stickerPageGridView.setAdapter(adapter);
        container.addView(stickerPageGridView);
        mActivePageViews.put(position, stickerPageGridView);
        return stickerPageGridView;
    }

    private void setGridViewLayoutProperties(GridViewWithHeaderAndFooter stickerPageGridView) {
        int gap = HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.facemoji_myfacemoji_grid_gap);
        stickerPageGridView.setGravity(Gravity.CENTER);
        stickerPageGridView.setNumColumns(GRID_COLUMN_NUMBER);
        stickerPageGridView.setVerticalSpacing(HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.facemoji_grid_item_vertical_space));
        stickerPageGridView.setHorizontalSpacing(HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.facemoji_grid_item_horizontal_space));
        stickerPageGridView.setVerticalScrollBarEnabled(false);

        stickerDimension = (int) ((float) (pagerHeight - gap * (GRID_ROW_NUMBER - 1)) / GRID_ROW_NUMBER);
        int topPadding = (pagerHeight - stickerDimension*GRID_ROW_NUMBER- gap * (GRID_ROW_NUMBER - 1))/2-5;
        ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(topPadding,topPadding);
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
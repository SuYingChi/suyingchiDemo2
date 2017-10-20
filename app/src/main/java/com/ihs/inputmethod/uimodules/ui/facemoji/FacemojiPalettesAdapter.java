package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;

import java.util.List;

class FacemojiPalettesAdapter extends PagerAdapter implements Recoverable {
    private FacemojiManager.FacemojiType facemojiType = FacemojiManager.FacemojiType.CLASSIC;
    private static String TAG = "FacemojiPalettesAdapter";
    private SparseArray<FacemojiPageGridView> mActivePageViews = new SparseArray<>();
    private FacemojiLayoutParams mImageLayoutParams;

    private int mActivePosition = 0;
    private FacemojiPalettesView mFacemojiPalettesView;

    public FacemojiPalettesAdapter(FacemojiPalettesView facemojiPalettesView,
                                   FacemojiLayoutParams imageLayoutParams) {
        mFacemojiPalettesView = facemojiPalettesView;
        mImageLayoutParams = imageLayoutParams;
    }

    public void setFacemojiType(FacemojiManager.FacemojiType facemojiType) {
        this.facemojiType = facemojiType;
    }

    @Override
    public int getCount() {
        return FacemojiManager.getInstance().getTotalPageCount(facemojiType);
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
        FacemojiPageGridView view = mActivePageViews.get(position);
        if (view != null) {
            container.addView(view);
            return view;
        }
        mActivePageViews.remove(position);
        final LayoutInflater inflater = LayoutInflater.from(container.getContext());
        final FacemojiPageGridView gifPageGridView = (FacemojiPageGridView) inflater.inflate(R.layout.facemoji_page_view, container, false);

        List<FacemojiSticker> data = FacemojiManager.getInstance().getDataFromPagePosition(facemojiType, position);

        int height;
        int width;
        if (data.get(0).getWidth() == data.get(0).getHeight()){
            mImageLayoutParams.setPageGridViewProperties(gifPageGridView);
            height = width = Math.min(mImageLayoutParams.getViewWidth(),mImageLayoutParams.getViewHeight());
        }else {
            Resources resources = HSApplication.getContext().getResources();
            int verticalSpacing = resources.getDimensionPixelSize(R.dimen.facemoji_keyboard_grid_vertical_space);
            int horizontalSpacing = resources.getDimensionPixelSize(R.dimen.facemoji_keyboard_grid_horizontal_space);
            int columns = 3;
            gifPageGridView.setVerticalSpacing(verticalSpacing);
            gifPageGridView.setHorizontalSpacing(horizontalSpacing);
            gifPageGridView.setNumColumns(columns);

            height = (container.getMeasuredHeight() - verticalSpacing) / 2;
            width = (container.getMeasuredWidth() - horizontalSpacing * (columns - 1)) / columns;
        }

        FacemojiPageGridAdapter adapter = new FacemojiPageGridAdapter(
                data,
                mFacemojiPalettesView,
                container.getContext(),
                width, height);
        gifPageGridView.setAdapter(adapter);
        container.addView(gifPageGridView);
        mActivePageViews.put(position, gifPageGridView);
        return gifPageGridView;
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        container.removeView(mActivePageViews.get(position));
        mActivePageViews.remove(position);
    }


    private boolean isCurrentPage(final int position) {
        return position == mFacemojiPalettesView.getCurrentPagerPosition();
    }

    public void pauseAnimation() {
        stopAnimation(mActivePosition);
    }

    public void resumeAnimation() {
        startAnimation(mActivePosition);
    }

    public void startAnimation(final int pageId) {
        HSLog.d("start animation for page " + pageId);

        FacemojiPageGridView facemojiPageGridView = mActivePageViews.get(pageId);
        if (facemojiPageGridView == null) {
            return;
        }

        if (facemojiPageGridView.getChildCount() == 0) {
            facemojiPageGridView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    HSLog.d("start animation for page delayed... " + pageId);
                    startAnimation(pageId);
                }
            }, 50);
        }

        HSLog.d("child count" + facemojiPageGridView.getChildCount());

        for (int i = 0; i < facemojiPageGridView.getChildCount(); ++i) {
            FacemojiView facemojiView = (FacemojiView) facemojiPageGridView.getChildAt(i);
            facemojiView.startAnimation();
        }
    }

    public void stopAllAnimations() {
        for (int i = 0; i < mActivePageViews.size(); ++i) {
            stopAnimation(i);
        }
    }

    /**
     * Stop animation of the page
     *
     * @param pageId
     */
    private void stopAnimation(int pageId) {
        FacemojiPageGridView facemojiPageGridView = mActivePageViews.get(pageId);

        if (facemojiPageGridView == null) {
            HSLog.d("failed to stop animation for page " + pageId);
            return;
        }

        HSLog.d("stop animation for page " + pageId);
        HSLog.d("child count" + facemojiPageGridView.getChildCount());

        for (int i = 0; i < facemojiPageGridView.getChildCount(); ++i) {
            FacemojiView facemojiView = (FacemojiView) facemojiPageGridView.getChildAt(i);
            facemojiView.stopAnimation();
        }
    }

    @Override
    public void save() {
        for (int i = 0, len = mActivePageViews.size(); i < len; i++) {
            mActivePageViews.valueAt(i).save();
        }
    }

    @Override
    public void restore() {
        for (int i = 0, len = mActivePageViews.size(); i < len; i++) {
            mActivePageViews.valueAt(i).restore();
        }
    }

    @Override
    public void release() {
        for (int i = 0, len = mActivePageViews.size(); i < len; i++) {
            mActivePageViews.valueAt(i).release();
        }
    }

    @Override
    public Recoverable.State currentState() {
        throw new UnsupportedOperationException();
    }

}
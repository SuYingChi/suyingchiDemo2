package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.res.Configuration;
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
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.AnimationLayout;

import java.util.List;

class FacemojiPalettesAdapter extends PagerAdapter implements Recoverable {
    // --Commented out by Inspection (18/1/11 下午2:41):private static String TAG = "FacemojiPalettesAdapter";
    private SparseArray<FacemojiPageGridView> mActivePageViews = new SparseArray<>();
    // --Commented out by Inspection (18/1/11 下午2:41):private FacemojiLayoutParams mImageLayoutParams;
    private int orientation;

    private int mActivePosition = 0;
    private FacemojiPalettesView mFacemojiPalettesView;

    public FacemojiPalettesAdapter(FacemojiPalettesView facemojiPalettesView,
                                   FacemojiLayoutParams imageLayoutParams) {
        mFacemojiPalettesView = facemojiPalettesView;
        orientation = facemojiPalettesView.getResources().getConfiguration().orientation;
    }

    @Override
    public int getCount() {
        return FacemojiManager.getInstance().getTotalPageCount(FacemojiManager.ShowLocation.Keyboard,mFacemojiPalettesView.getResources().getConfiguration().orientation);
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

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void clear() {
//        mActivePageViews.clear();
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

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

        List<FacemojiSticker> data = FacemojiManager.getInstance().getDataFromPagePosition(position, FacemojiManager.ShowLocation.Keyboard,orientation);

        int height;
        int width;
        Resources resources = HSApplication.getContext().getResources();
        int verticalSpacing = resources.getDimensionPixelSize(R.dimen.facemoji_keyboard_grid_vertical_space);
        int horizontalSpacing = resources.getDimensionPixelSize(R.dimen.facemoji_keyboard_grid_horizontal_space);
        int columns = 3;
        gifPageGridView.setVerticalSpacing(verticalSpacing);
        gifPageGridView.setHorizontalSpacing(horizontalSpacing);
        gifPageGridView.setNumColumns(columns);

        if (container.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            width = (container.getMeasuredWidth() - horizontalSpacing * (columns - 1)) / columns;
            height = container.getMeasuredHeight();
        } else {
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


// --Commented out by Inspection START (18/1/11 下午2:41):
//    private boolean isCurrentPage(final int position) {
//        return position == mFacemojiPalettesView.getCurrentPagerPosition();
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

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

        FacemojiPageGridAdapter adapter = (FacemojiPageGridAdapter) facemojiPageGridView.getOriginalAdapter();
        if (adapter != null){
            adapter.setAllowPlayAnim(true);
        }

        for (int i = 0; i < facemojiPageGridView.getChildCount(); ++i) {
            View view =  facemojiPageGridView.getChildAt(i);
            if (view instanceof AnimationLayout){
                ((FacemojiAnimationView)((AnimationLayout) view).getChildAt(0)).startAnim();
            }
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

        FacemojiPageGridAdapter adapter = (FacemojiPageGridAdapter) facemojiPageGridView.getOriginalAdapter();
        if (adapter != null){
            adapter.setAllowPlayAnim(false);
        }

        HSLog.d("stop animation for page " + pageId);
        HSLog.d("child count" + facemojiPageGridView.getChildCount());

        for (int i = 0; i < facemojiPageGridView.getChildCount(); ++i) {
            View view =  facemojiPageGridView.getChildAt(i);
            if (view instanceof AnimationLayout){
                ((FacemojiAnimationView)((AnimationLayout) view).getChildAt(0)).stopAnim();
            }
        }
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void notifyDownloaded(int position) {
//        FacemojiPageGridView gridView = mActivePageViews.get(position);
//        if (gridView != null) {
//            FacemojiPageGridAdapter adapter = (FacemojiPageGridAdapter) gridView.getOriginalAdapter();
//            if (adapter != null){
//                adapter.setData(FacemojiManager.getInstance().getDataFromPagePosition(position, FacemojiManager.ShowLocation.Keyboard,orientation));
//                adapter.notifyDataSetChanged();
//            }
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

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
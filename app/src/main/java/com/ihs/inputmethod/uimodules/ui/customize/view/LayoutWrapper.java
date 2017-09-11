package com.ihs.inputmethod.uimodules.ui.customize.view;

/**
 * Created by guonan.lv on 17/9/1.
 */

import android.animation.ObjectAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.BuildConfig;

public class LayoutWrapper {
    private final static int TRANSLATE_DURATION_MILLIS = 300;
    private final static int DEFAULT_LEAST_SCROLL_HEIGHT = 10;
    private static final java.lang.String TAG = "LayoutWrapper";
    @SuppressWarnings("PointlessBooleanExpression")
    private final boolean DEBUG = false && BuildConfig.DEBUG;
    private final Interpolator mInterpolator = new DecelerateInterpolator();
    private final int mBottomHeight;
    private int mLeastScrollHeight = DEFAULT_LEAST_SCROLL_HEIGHT;
    private ViewGroup mLayout;

    private boolean mVisible;
    private boolean mIsScrollToTheBottom = false;
    private boolean mHasRecover = true;
    private boolean mLayoutManagerBottom = false;

    private int mLastVisibleItemPosition;
    private int mLastPos;

    private ScrollListener mScrollListener;
    private NoBottomScrollListener mNoBottomScrollListener;

    public LayoutWrapper(ViewGroup layout, int bottomHeight, int leastScrollHeight) {
        mLayout = layout;
        mLeastScrollHeight = leastScrollHeight;
        mBottomHeight = bottomHeight;
        init();
    }

    private void init() {
        mScrollListener = new ScrollListener();
        mNoBottomScrollListener = new NoBottomScrollListener();
        mVisible = true;
    }

    public ViewGroup getLayout() {
        return mLayout;
    }

    void restoreRecyclerView(RecyclerView recyclerView, int firstPosition) {
        if (DEBUG) {
            HSLog.i(TAG, "mHasRecover  " + mHasRecover + "   " + firstPosition);
        }
        if (!mHasRecover) {
            recyclerView.clearAnimation();
            recyclerView.animate().setInterpolator(mInterpolator)
                    .setDuration(0)
                    .translationY(0);
            mHasRecover = true;
        }
        LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        if (firstVisibleItemPosition < firstPosition) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    private void restore(ViewGroup viewGroup) {
        if (DEBUG) {
            HSLog.i(TAG, "restore  ");
        }
        if (!mHasRecover) {
            viewGroup.setTranslationY(0);
        } else {
            if (viewGroup.getTranslationY() != 0) {
                viewGroup.animate().setInterpolator(new DecelerateInterpolator())
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationY(0);
                mHasRecover = true;
            }
        }
        mHasRecover = true;
    }

    private void restoreSmoothly(ViewGroup viewGroup) {
        if (DEBUG) {
            HSLog.i(TAG, "restoreSmoothly  ");
        }
        if (!mHasRecover) {
            viewGroup.animate().setInterpolator(new DecelerateInterpolator())
                    .setDuration(TRANSLATE_DURATION_MILLIS)
                    .translationY(0);
            mHasRecover = true;
        }
    }

    public void show() {
        mVisible = false;
        mIsScrollToTheBottom = false;
        toggle(true, false, false, null);
    }

    public void show(ViewGroup viewGroup, boolean isBottom) {
        mIsScrollToTheBottom = isBottom;
        toggle(true, true, false, viewGroup);
    }

    public void hide(ViewGroup viewGroup) {
        mIsScrollToTheBottom = false;
        toggle(false, true, false, viewGroup);
    }

    @SuppressWarnings("ResourceType")
    private void toggle(final boolean visible, final boolean animate, boolean force, final ViewGroup viewGroup) {
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = mLayout.getHeight();
            if (height == 0 && !force) {
                mLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                        toggle(visible, animate, true, viewGroup);
                        return true;
                    }
                });
                return;
            }
            int translationY = visible ? 0 : mBottomHeight;
            if (DEBUG) {
                HSLog.i(TAG, "isShow " + visible + " mIsScrollToTheBottom " + mIsScrollToTheBottom);
            }
            if (mIsScrollToTheBottom && viewGroup != null) {
                if (visible) {
                    mHasRecover = false;
                    layoutAnimate(viewGroup, -mBottomHeight);
                } else {
                    mHasRecover = true;
                    layoutAnimate(viewGroup, 0);
                }
            }
            if (animate) {
                animate(translationY);
            } else {
                mLayout.setTranslationY(translationY);
            }
        }
    }

    private void layoutAnimate(ViewGroup viewGroup, float translationY) {
        if (DEBUG) {
            HSLog.i(TAG, "show viewGroup " + translationY);
        }
        ObjectAnimator localObjectAnimator = ObjectAnimator.ofFloat(viewGroup, View.TRANSLATION_Y,
                viewGroup.getTranslationY(), translationY);
        localObjectAnimator.setDuration(300L);
        localObjectAnimator.setInterpolator(mInterpolator);
        localObjectAnimator.start();
    }

    private void animate(float translationY) {
        ObjectAnimator localObjectAnimator = ObjectAnimator.ofFloat(mLayout, View.TRANSLATION_Y,
                mLayout.getTranslationY(), translationY);
        localObjectAnimator.setDuration(300L);
        localObjectAnimator.setInterpolator(mInterpolator);
        localObjectAnimator.start();
    }

    public void attachToRecyclerView(RecyclerView recyclerView, boolean hasBottom) {
        if (hasBottom) {
        recyclerView.addOnScrollListener(mScrollListener);
        } else {
            recyclerView.addOnScrollListener(mNoBottomScrollListener);
        }
    }

    private void dump() {
        if (DEBUG) {
            HSLog.i(TAG, "mHasRecover  " + mHasRecover + "  mVisible " + mVisible + " mIsScrollToTheBottom "
                    + mIsScrollToTheBottom + "   mLayoutManagerBottom " + mLayoutManagerBottom);
        }
    }

    private class NoBottomScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy > mLeastScrollHeight) {
                hide(recyclerView);
            }
            if (dy < -mLeastScrollHeight) {
                show(recyclerView, false);
            }
        }
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                int lastCompletelyVisibleItemPosition = linearManager.findLastCompletelyVisibleItemPosition();
                mLastVisibleItemPosition = linearManager.findLastVisibleItemPosition();
                mLastPos = linearManager.getItemCount() - 1;
                if (DEBUG) {
                    HSLog.i(TAG, "mLastVisibleItemPosition " + mLastVisibleItemPosition + " mLastPos " + mLastPos + " mCompletelyVisibleItemPosition " + lastCompletelyVisibleItemPosition);
                }
                if (mLastPos == lastCompletelyVisibleItemPosition && mLastPos >= 6) {
                    show(recyclerView, true);
                    mLayoutManagerBottom = true;
                } else {
                    if (mLastVisibleItemPosition != mLastPos) {
                        //  HSLog.i(TAG, "restoreSmoothly");
                        restoreSmoothly(recyclerView);
                    }
                    mLayoutManagerBottom = false;
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (DEBUG) {
                dump();
                HSLog.i(TAG, "dy  " + dy);
            }
            if (mVisible && mLayoutManagerBottom && mLastVisibleItemPosition != mLastPos) {
                if (DEBUG) {
                    HSLog.i(TAG, "restore onScrolled");
                }
                restore(recyclerView);
                mLayoutManagerBottom = false;
            } else if (dy > mLeastScrollHeight && !mLayoutManagerBottom) {

            } else if (dy < -mLeastScrollHeight) {
                show(recyclerView, false);
            }
        }
    }
}

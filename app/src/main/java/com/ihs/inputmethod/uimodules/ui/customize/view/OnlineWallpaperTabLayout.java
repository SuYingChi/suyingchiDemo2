package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by guonan.lv on 17/9/2.
 */

public class OnlineWallpaperTabLayout extends TabLayout {

    public OnScrollListener mOnScrollListener;
    private boolean mHasScrolledLeft;
    private boolean mHasScrolledRight;

    public interface OnScrollListener {
        void onScrollFinished(boolean isScrollLeft, boolean isScrollRight);
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }

    public OnlineWallpaperTabLayout(Context context) {
        this(context, null);
    }

    public OnlineWallpaperTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OnlineWallpaperTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == KeyEvent.ACTION_UP) {
            if (mHasScrolledLeft) {
                mOnScrollListener.onScrollFinished(true, false);
            } else if (mHasScrolledRight) {
                mOnScrollListener.onScrollFinished(false, true);
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (l > oldl) {
            mHasScrolledLeft = true;
            mHasScrolledRight = false;
        }
        if (l < oldl) {
            mHasScrolledLeft = false;
            mHasScrolledRight = true;
        }
    }
}

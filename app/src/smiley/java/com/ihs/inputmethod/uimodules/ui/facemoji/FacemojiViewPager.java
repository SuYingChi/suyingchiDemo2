package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.ihs.commons.utils.HSLog;

public class FacemojiViewPager extends ViewPager {

    private float mLastDownX;
    private float mLastDownY;

    private static final int MOVE_ACTION_LIMIT_DISTANCE = 10;

    public FacemojiViewPager(Context context) {
        super(context);
    }

    public FacemojiViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int getMoveDistance(final float x, final float y) {
        final float xDis = Math.abs(x - mLastDownX);
        final float yDis = Math.abs(y - mLastDownY);
        return (int) (Math.sqrt(xDis * xDis + yDis * yDis));
    }

    private void onActionUp(final boolean isSelected) {
        releaseSelectedFacemoji(isSelected);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getAction();

        final float x = event.getX();
        final float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                HSLog.d("action up");
                onActionUp(false);
                break;

            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 用于拦截手势事件的，每个手势事件都会先调用这个方法。Layout里的onInterceptTouchEvent默认返回值是false,
     * 这样touch事件会传递到childview控件 ，如果返回false子控件可以响应，否则了控件不响应，这里主要是拦截子控件的响应，
     * 对ViewGroup不管返回值是什么都会执行onTouchEvent
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {

        final int action = arg0.getAction();

        final float x = arg0.getX();
        final float y = arg0.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                HSLog.d("action down");
                mLastDownX = x;
                mLastDownY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                HSLog.d("action move");
                if (getMoveDistance(x, y) > MOVE_ACTION_LIMIT_DISTANCE) {
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                HSLog.d("action up");
                onActionUp(true);
                break;

            default:
                break;
        }

        return super.onInterceptTouchEvent(arg0);
    }

    private void releaseSelectedFacemoji(final boolean isSelected) {
        // Get current gridview
        FacemojiPageGridView gridView;
        FacemojiView facemojiView;

        for (int gridViewId = 0; gridViewId < getChildCount(); ++gridViewId) {
            gridView = (FacemojiPageGridView) getChildAt(gridViewId);

            for (int i = 0; i < gridView.getChildCount(); ++i) {
                facemojiView = (FacemojiView) gridView.getChildAt(i);

                // Release pressed facemoji
                if (facemojiView.isFacemojiPressed()) {
                    facemojiView.release(isSelected);
                    return;
                }
            }
        }
    }
}
package com.ihs.inputmethod.uimodules.ui.theme;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jixiang on 16/9/26.
 *
 * Float Action Button Auto Hide Behavior
 */

public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {
    private boolean hide;
    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
        hide = false;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        //child -> Floating Action Button
        if (!hide && (dyUnconsumed > 0 || dyConsumed > 0)) {
            hide = true;
//            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
//            int fab_bottomMargin = layoutParams.bottomMargin;
//            child.animate().translationY(child.getHeight() + fab_bottomMargin).setInterpolator(new LinearInterpolator()).start();
            child.hide();
        } else if (hide && (dyConsumed <0 || dyUnconsumed < 0)) {
            hide = false;
            child.show();
//            child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
        }

    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }
}
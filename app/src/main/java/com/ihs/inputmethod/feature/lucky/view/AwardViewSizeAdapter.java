package com.ihs.inputmethod.feature.lucky.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.inputmethod.uimodules.R;


/**
 * Scales {@link AwardView} to adapt different screen sizes.
 */
public class AwardViewSizeAdapter extends FrameLayout {

    private final int mDesignHeight;
    private final int mDesignWidth;

    public AwardViewSizeAdapter(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources res = context.getResources();
        mDesignHeight = res.getDimensionPixelSize(R.dimen.lucky_award_view_design_height);
        mDesignWidth = res.getDimensionPixelSize(R.dimen.lucky_award_view_design_width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        View awardView = getChildAt(0);

        float scale = Math.min((float) getHeight() / mDesignHeight, (float) getWidth() / mDesignWidth);
        awardView.setPivotX(getWidth() / 2);
        awardView.setPivotY(getHeight() / 2);
        awardView.setScaleX(scale);
        awardView.setScaleY(scale);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        ((AwardView) getChildAt(0)).setSizeAdapter(this);
    }
}

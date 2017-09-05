package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by guonan.lv on 17/9/5.
 */

public class FixedRatioLayout extends RelativeLayout {

    private float mAspectRatio;

    public FixedRatioLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedRatioLayout);
        mAspectRatio = a.getFloat(R.styleable.FixedRatioLayout_aspectRatio, -1f);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeightSpec = heightMeasureSpec;
        if (mAspectRatio > 0f) {
            int height = (int) Math.ceil(width / mAspectRatio);
            desiredHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        }

        // Note that RelativeLayout works differently from common ViewGroups. Super must be called or its children
        // will not be laid out correctly. See https://groups.google.com/forum/#!topic/android-developers/WpR0gtfctgU.
        super.onMeasure(widthMeasureSpec, desiredHeightSpec);
    }
}
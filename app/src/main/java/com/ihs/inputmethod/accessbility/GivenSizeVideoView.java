package com.ihs.inputmethod.accessbility;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by Arthur on 17/1/5.
 */

public class GivenSizeVideoView extends VideoView {
    private int width = 0, height = 0;

    public GivenSizeVideoView(Context context) {
        super(context);
    }

    public GivenSizeVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GivenSizeVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setViewSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}

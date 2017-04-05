package com.ihs.inputmethod.uimodules.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.inputmethod.uimodules.R;


public class RoundedCornerLayout extends FrameLayout {
    private final static int CORNER_RADIUS = 3;
    private float cornerRadius;
    private boolean isCircle;

    public RoundedCornerLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public RoundedCornerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public RoundedCornerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.RoundedCornerLayout, 0, 0);
        try {
            cornerRadius = a.getDimensionPixelSize(R.styleable.RoundedCornerLayout_rcl_corner_radius, CORNER_RADIUS);
        } finally {
            a.recycle();
        }
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setCircle(boolean circle) {
        isCircle = circle;
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int count = canvas.save();
        final Path path = new Path();
        if (!isCircle) {
            path.addRoundRect(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), cornerRadius, cornerRadius, Path.Direction.CW);
        } else {
            float radius = Math.min(canvas.getWidth() / 2, canvas.getHeight() / 2);
            path.addCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, radius, Path.Direction.CW);
        }
        canvas.clipPath(path, Region.Op.REPLACE);

        canvas.clipPath(path);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(count);
    }


}
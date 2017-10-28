package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ihs.app.framework.HSApplication;

/**
 * Created by jixiang on 17/10/23.
 */

public class FacemojiIndicatorView extends FrameLayout {
    private int indicatorViewVerticalSpacing = (int) (HSApplication.getContext().getResources().getDisplayMetrics().density * 7);

    public FacemojiIndicatorView(Context context) {
        this(context, null);
    }

    public FacemojiIndicatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FacemojiIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void updateIndicator(int currentPage, int totalPage) {
        removeAllViews();
        LinearLayout linearLayout = new LinearLayout(HSApplication.getContext());
        FrameLayout.LayoutParams layParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layParams.gravity = Gravity.CENTER;
        linearLayout.setLayoutParams(layParams);
        IndicatorView circleView;
        for (int i = 0; i < totalPage; i++) {
            circleView = new IndicatorView(HSApplication.getContext());
            circleView.setSelected(currentPage == i);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.rightMargin = (i != totalPage - 1) ? indicatorViewVerticalSpacing : 0;
            circleView.setLayoutParams(layoutParams);

            linearLayout.addView(circleView);
        }
        addView(linearLayout);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    private static class IndicatorView extends View {
        private Paint paint;
        private int radius;

        public IndicatorView(Context context) {
            this(context, null);
        }

        public IndicatorView(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            init();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            int measuredHeight = getMeasuredHeight();
            setMeasuredDimension(measuredHeight, measuredHeight);

            radius = (measuredHeight - getPaddingTop() - getPaddingBottom()) / 2;
        }

        private void init() {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setColor(isSelected() ? 0x88c3c3c3 : 0x88888888);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, paint);
        }
    }
}

package com.ihs.inputmethod.uimodules.ui.theme.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.ihs.inputmethod.uimodules.R;


/**
 * Created by chenyuanming on 11/11/2016.
 */

public class SectorProgressView extends View {
    private int spv_bg_color = Color.parseColor("#aa000000");
    private Paint ovalPaint;
    private Paint gapPaint;
    private Paint bgPaint;
    private RectF oval;
    private float gapWithOfRadiusPercent = .15f;

    private PaintFlagsDrawFilter paintFlagsDrawFilter;
    private RectF rect;

    private float startAngle = -90;
    private float sweepAngle = 0;
    private int spv_percent;
    int cornerRadius = 6;

    int centerX;
    int centerY;
    int ovalRadius;
    int sectorRadius;
    int sectorRadiusPercent;
    int w, h;
    private boolean drawCircle;

    public SectorProgressView(Context context) {
        super(context);
        init(context, null);
    }

    public SectorProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SectorProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SectorProgressView, 0, 0);

            try {
                spv_bg_color = a.getColor(R.styleable.SectorProgressView_spv_bg_color, Color.parseColor("#aa000000"));
                spv_percent = a.getInteger(R.styleable.SectorProgressView_spv_percent, 0);
                cornerRadius = a.getDimensionPixelOffset(R.styleable.SectorProgressView_spv_corner_radius, dp2px(3));
                gapWithOfRadiusPercent = a.getInt(R.styleable.SectorProgressView_spv_gap_width_of_radius_percent, 15) / 100.f;
                sectorRadiusPercent = a.getInt(R.styleable.SectorProgressView_spv_sector_radius_percent, 85);

            } finally {
                a.recycle();
            }
        }
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setDither(true);
        bgPaint.setColor(spv_bg_color);


        ovalPaint = new Paint();
        ovalPaint.setAntiAlias(true);
        ovalPaint.setDither(true);
        ovalPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));


        gapPaint = new Paint();
        gapPaint.setAntiAlias(true);
        gapPaint.setDither(true);
        gapPaint.setStyle(Paint.Style.STROKE);
        gapPaint.setStrokeWidth(50);
        gapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));


        updateArgs(spv_percent * 3.6f);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int paddingHorizontal = getPaddingLeft() + getPaddingRight() + 2;
        int paddingVertical = getPaddingTop() + getPaddingBottom() + 2;
        int ovalWidth = w - paddingHorizontal;
        int ovalHeight = h - paddingVertical;

        sectorRadius = (int) (Math.min(ovalWidth, ovalHeight) / 2 * (sectorRadiusPercent / 100f));//外侧圆
        ovalRadius = (int) Math.ceil(sectorRadius * (1 - gapWithOfRadiusPercent));//内侧圆
        centerX = w / 2;
        centerY = h / 2;
        this.w = w;
        this.h = h;
        oval = new RectF(centerX - ovalRadius, centerY - ovalRadius, centerX + ovalRadius, centerY + ovalRadius);
        rect = new RectF(0, 0, w, h);
        gapPaint.setStrokeWidth(gapWithOfRadiusPercent * sectorRadius);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int layerId = canvas.saveLayer(0, 0, w, h, null, Canvas.ALL_SAVE_FLAG);

        if(drawCircle){
            canvas.drawCircle(centerX, centerY,  sectorRadius / (sectorRadiusPercent / 100f), bgPaint);
        }else{
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint);
        }

        canvas.drawCircle(centerX, centerY, sectorRadius - gapPaint.getStrokeWidth() / 2, gapPaint);
        canvas.drawArc(oval, startAngle, sweepAngle, true, ovalPaint);
        canvas.restoreToCount(layerId);


    }


    private void refreshTheLayout() {
        invalidate();
        requestLayout();
    }


    public void setPercent(int percent) {
        setPercent(percent, 0);
    }

    public void setPercent(int percent, int animMillis) {
        final float degree = percent * 3.6f;
        ValueAnimator animator = ValueAnimator.ofFloat(0, degree);
        animator.setDuration(animMillis);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {

                post(new Runnable() {
                    @Override
                    public void run() {
                        updateArgs((Float) animation.getAnimatedValue());
                        refreshTheLayout();

                    }
                });
            }
        });
        animator.start();

    }


    private void updateArgs(float degree) {
        startAngle = -90;
        sweepAngle = degree;
    }


    private int dp2px(float dp) {
        float m = getResources().getDisplayMetrics().density;
        return (int) (dp * m);
    }

    public void setDrawCircle(boolean drawCircle){
        this.drawCircle=drawCircle;
    }
}


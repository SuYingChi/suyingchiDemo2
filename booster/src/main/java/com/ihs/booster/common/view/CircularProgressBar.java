package com.ihs.booster.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.ihs.booster.constants.AnimationConstant;

/**
 * 圆环进度条
 * <p>
 * by bojia 2014-04-17
 */
public class CircularProgressBar extends View {
    protected Paint paint = new Paint();
    private float startAngle = AnimationConstant.MINANGLE;
    private float sweepAngle = 0f;
    private int barWidth = 4;
    private int barBackColor = Color.TRANSPARENT;
    private int barForeColor = Color.WHITE;
    private int viewBackColor = Color.TRANSPARENT;
    private PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

    public CircularProgressBar(Context context) {
        super(context);
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * setTopClipAngle: 设置进度条宽度，默认是 4px.
     * <p>
     *
     * @param barWidth 进度条宽度
     * @return void
     */
    public void setBarWidth(int barWidth) {
        this.barWidth = dip2px(barWidth);
        invalidate();
    }

    private int dip2px(int value) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return (int) (value * displayMetrics.density + 0.5f);
    }

    public float getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
    }

    /**
     * setProgress: 设置进度条进度 （0-360）.
     * <p>
     *
     * @param sweepAngle 进度条进度
     * @return void
     */
    public void setSweepAngle(float sweepAngle) {
        this.sweepAngle = sweepAngle > 360 ? 360 : sweepAngle;
        this.sweepAngle = this.sweepAngle < 0 ? 0 : this.sweepAngle;
        invalidate();
    }

    /**
     * setBarBackColor: 设置进度条背景色，默认是 #d8d7d7.
     * <p>
     *
     * @param barBackColor 进度条背景色
     * @return void
     */
    public void setBarBackColor(int barBackColor) {
        this.barBackColor = barBackColor;
        invalidate();
    }

    /**
     * setBarForeColor: 设置进度条前景色，默认是 Color.RED.
     * <p>
     *
     * @param barForeColor 进度条前景色
     * @return void
     */
    public void setBarForeColor(int barForeColor) {
        this.barForeColor = barForeColor;
        invalidate();
    }

    /**
     * setViewBackColor: 设置进度条内圆背景色，默认是 Color.WHITE,可设置为 Color.TRANSPARENT.
     * <p>
     *
     * @param viewBackColor 进度条内圆背景色
     * @return void
     */
    public void setViewBackColor(int viewBackColor) {
        this.viewBackColor = viewBackColor;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawArc(canvas, paint, barBackColor, 360);
        drawArc(canvas, paint, barForeColor, sweepAngle);
    }

    private void drawArc(Canvas canvas, Paint paint, int color, float sweepAngle) {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(barWidth);
        paint.setColor(color);
        RectF oval = new RectF(barWidth / 2, barWidth / 2, getRight() - getLeft() - barWidth / 2, getRight() - getLeft() - barWidth / 2);
        canvas.drawArc(oval, startAngle, sweepAngle, false, paint);
    }

    private boolean isViewBackTransparent() {
        return viewBackColor == Color.TRANSPARENT;
    }
}

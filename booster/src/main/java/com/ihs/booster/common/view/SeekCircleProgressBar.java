package com.ihs.booster.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * 圆环进度条
 * <p>
 * by bojia 2014-04-17
 */
public class SeekCircleProgressBar extends View {

    protected float mRadius;
    protected int mMaxProgress = 100;
    protected float mCenterX;
    protected float mCenterY;
    private float startAngle = -90.0f;
    private int mCurProgress = 0;
    private int barWidth = 4;
    private int barBackColor = Color.parseColor("#d8d7d7");
    private int barForeColor = Color.WHITE;
    private int viewBackColor = Color.TRANSPARENT;
    private float mSectionRatio = 11.0f;
    private RectF mSectionRect = new RectF();
    private Paint mPaint;

    {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public SeekCircleProgressBar(Context context) {
        super(context);
    }

    public SeekCircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMaxProgress(int mMaxProgress) {
        this.mMaxProgress = mMaxProgress;
    }

    public void setSectionRatio(float mSectionRatio) {
        this.mSectionRatio = mSectionRatio;
    }

    /**
     * setTopClipAngle: 设置进度条宽度，默认是 4dp.
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

    /**
     * setProgress: 设置进度条进度 （0-100）.
     * <p>
     *
     * @param progress 进度条进度
     * @return void
     */
    public void setProgress(int progress) {
        this.mCurProgress = progress > 100 ? 100 : progress;
        this.mCurProgress = this.mCurProgress < 0 ? 0 : this.mCurProgress;
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
        updateDimensions(getWidth(), getHeight());
        canvas.translate(mCenterX, mCenterY);
        float rotation = 360.0f / (float) mMaxProgress;
        for (int i = 0; i < mMaxProgress; ++i) {
            canvas.save();
            canvas.rotate((float) i * rotation);
            canvas.translate(0, -mRadius);
            mPaint.setColor(i < mCurProgress ? barForeColor : barBackColor);
            canvas.drawRect(mSectionRect, mPaint);
            canvas.restore();
        }
    }

    private void updateDimensions(int width, int height) {
        // Update center position
        mCenterX = width / 2.0f;
        mCenterY = height / 2.0f;

        // Find shortest dimension
        int diameter = Math.min(width, height);

        float outerRadius = diameter / 2;
        float sectionWidth = barWidth / mSectionRatio;

        mRadius = outerRadius - barWidth / 2;
        mSectionRect.set(-sectionWidth / 2, -barWidth / 2, sectionWidth / 2, barWidth / 2);
    }
}

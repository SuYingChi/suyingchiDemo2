package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.LauncherAnimUtils;
import com.ihs.inputmethod.feature.common.CommonUtils;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by guonan.lv on 17/9/5.
 */

public class LoadingProgressBar extends View {

    private static final int GREEN = 0xff50ea3d; // Middle
    private static final int YELLOW = 0xfff8bb2d; // Left
    private static final int BLUE = 0xff3888f8; // Right

    private static final int LOADING_TEXT_OFFSET_Y_PX = CommonUtils.pxFromDp(13.3f);

    private float mBigCircleRadius;
    private float mSmallCircleRadius;
    private float mBigCircleDist;
    private float mSmallCircleDist;
    private float mVibrateRange;
    private boolean mShouldPrepare;
    private AnimationState mState;
    private int mPrepareHeight;
    private int mLoadingHeight;

    private enum AnimationState {
        PREPARE,
        LOADING,
    }

    private AnimationDelegate mAnimDelegate;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LoadingProgressBar(Context context) {
        this(context, null);
    }

    public LoadingProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadingProgressBar);
        mBigCircleRadius = a.getDimension(R.styleable.LoadingProgressBar_prepareCircleRadius, CommonUtils.pxFromDp(10));
        mSmallCircleRadius = a.getDimension(R.styleable.LoadingProgressBar_loadingCircleRadius, CommonUtils.pxFromDp(5));
        mBigCircleDist = a.getDimension(R.styleable.LoadingProgressBar_prepareCircleDistance, CommonUtils.pxFromDp(30));
        mSmallCircleDist = a.getDimension(R.styleable.LoadingProgressBar_loadingCircleDistance, CommonUtils.pxFromDp(15));
        mVibrateRange = a.getDimension(R.styleable.LoadingProgressBar_loadingCircleVibrateRange, CommonUtils.pxFromDp(3));
        mShouldPrepare = a.getBoolean(R.styleable.LoadingProgressBar_shouldPrepare, true);
        a.recycle();
        mState = mShouldPrepare ? AnimationState.PREPARE : AnimationState.LOADING;
        mPrepareHeight = (int) ((mBigCircleDist * Math.sin(Math.toRadians(60)) + mBigCircleRadius) * 2);
        mLoadingHeight = (int) ((mSmallCircleRadius + mVibrateRange) * 2);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w > 0 && h > 0 && !mShouldPrepare) {
            startLoadingAnimation();
        }
        if (mAnimDelegate != null) {
            mAnimDelegate.onCenterChanged(w / 2, getHeight(mState) / 2);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(getHeight(mState), MeasureSpec.EXACTLY);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    private int getHeight(AnimationState state) {
        if (state == AnimationState.PREPARE) {
            return mPrepareHeight;
        } else if (state == AnimationState.LOADING) {
            return mLoadingHeight + LOADING_TEXT_OFFSET_Y_PX;
        }
        return 0;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void prepare() {
//        mState = AnimationState.PREPARE;
//        try {
//            requestLayout();
//        } catch (IllegalArgumentException e) {
//            throw new RuntimeException("Exception in prepare()");
//        }
//        if (mAnimDelegate != null) {
//            mAnimDelegate.stop();
//        }
//        if (mAnimDelegate == null || !(mAnimDelegate instanceof PrepareAnimationDelegate)) {
//            mAnimDelegate = new PrepareAnimationDelegate(getWidth() / 2, mPrepareHeight / 2, this, mBigCircleRadius, mBigCircleDist);
//        }
//        mAnimDelegate.postProgress(0f);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void postPrepareAnimationProgress(float progress) {
//        if (mAnimDelegate instanceof PrepareAnimationDelegate) {
//            mAnimDelegate.postProgress(progress);
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    /**
     * Start the animation for ongoing loading process.
     * If the animation delegate is already LoadingAnimationDelegate and running, this method will do nothing
     */
    public void startLoadingAnimation() {
        // Can not get the width of self at now, wait for onSizeChanged() call this method again
        if (getWidth() == 0) {
            return;
        }
        mState = AnimationState.LOADING;
        try {
            requestLayout();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Exception in startLoadingAnimation()");
        }
        // If animation delegate is null, Create a LoadingAnimationDelegate
        if (mAnimDelegate == null) {
            mAnimDelegate = new LoadingAnimationDelegate(getWidth() / 2, mLoadingHeight / 2 + LOADING_TEXT_OFFSET_Y_PX,
                    this, mSmallCircleRadius, mSmallCircleDist, mVibrateRange);
        }
        // If animation delegate is PrepareAnimationDelegate, Stop the animation and create a LoadingAnimationDelegate
        if (mAnimDelegate instanceof PrepareAnimationDelegate) {
            mAnimDelegate.stop();
            mAnimDelegate = new LoadingAnimationDelegate(getWidth() / 2, mLoadingHeight / 2 + LOADING_TEXT_OFFSET_Y_PX,
                    this, mSmallCircleRadius, mSmallCircleDist, mVibrateRange);
        }
        // If animation is not running, Start it
        if (!mAnimDelegate.isRunning()) {
            mAnimDelegate.start();
        }
    }

    /**
     * Stop the animation
     */
    public void stopAnimation() {
        if (mAnimDelegate != null) {
            mAnimDelegate.stop();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mAnimDelegate != null) {
            mAnimDelegate.draw(canvas, mPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    private abstract static class AnimationDelegate {
        /**
         * Center coordinates of the green circle in the middle.
         */
        protected float mCx, mCy;
        protected View mParent;

        AnimationDelegate(float cx, float cy, View parent) {
            mCx = cx;
            mCy = cy;
            this.mParent = parent;
        }

        void onCenterChanged(int cx, int cy) {
            mCx = cx;
            mCy = cy;
        }

        /**
         * Start the animation with its own choreography.
         */
        abstract void start();

        /**
         * Manually instruct the animation to draw its look at given time slice.
         *
         * @param progress A float point number between 0f and 1f
         */
        abstract void postProgress(float progress);

        /**
         * Be sure to cancel animation to avoid memory leak.
         */
        abstract void stop();

        abstract void draw(Canvas canvas, Paint paint);

        abstract boolean isRunning();
    }

    private static class PrepareAnimationDelegate extends AnimationDelegate {
        private static final float SIN_60 = (float) Math.sin(Math.toRadians(60));
        private static final float COS_60 = (float) Math.cos(Math.toRadians(60));

        private float mRadius;
        private float mInitDist;

        private float mAnimProgress;
        private ValueAnimator mProgressAnim;

        PrepareAnimationDelegate(float cx, float cy, View parent, float radius, float dist) {
            super(cx, cy, parent);
            this.mRadius = radius;
            this.mInitDist = dist;
        }

        @Override
        public void start() {
            stop();
            mProgressAnim = LauncherAnimUtils.ofFloat(mParent, 0f, 1f);
            mProgressAnim.setInterpolator(new OvershootInterpolator());
            mProgressAnim.setDuration(1000);
            mProgressAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    HSLog.d("LoadingProgressBar", "Prepare animation is running");
                    mAnimProgress = (float) animation.getAnimatedValue();
                    mParent.invalidate();
                }
            });
            mProgressAnim.start();
        }

        @Override
        public void postProgress(float progress) {
            mAnimProgress = progress;
            mParent.invalidate();
        }

        @Override
        public void stop() {
            if (mProgressAnim != null && mProgressAnim.isRunning()) {
                mProgressAnim.cancel();
            }
        }

        @Override
        public boolean isRunning() {
            if (mProgressAnim == null) {
                return false;
            }
            return mProgressAnim.isRunning();
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            float animProgress1 = fixProgress(mAnimProgress * 3);
            float animProgress2 = fixProgress((mAnimProgress - 1f / 3f) * 3);
            float animProgress3 = fixProgress((mAnimProgress - 1f / 2f) * 2);
            float curX = mCx;
            float curY = 2 * mCy - mCy * mAnimProgress;
            paint.setColor(GREEN);
            paint.setAlpha((int) (0xFF * animProgress1));
            canvas.drawCircle(curX, curY, mRadius, paint);

            float currentDist = mInitDist * (1 - animProgress2);
            float x = curX - COS_60 * currentDist;
            float y = curY + SIN_60 * currentDist;
            paint.setColor(YELLOW);
            paint.setAlpha((int) (0xFF * animProgress2));
            canvas.drawCircle(x, y, mRadius, paint);

            currentDist = (float) (mInitDist * (1 - animProgress3) * 0.7);
            x = curX + COS_60 * currentDist;
            y = curY + SIN_60 * currentDist;
            paint.setColor(BLUE);
            paint.setAlpha((int) (0xFF * animProgress3));
            canvas.drawCircle(x, y, mRadius, paint);
        }

        private float fixProgress(float progress) {
            if (progress < 0) {
                return 0;
            }
            if (progress > 1) {
                return 1;
            }
            return progress;
        }
    }

    private static class LoadingAnimationDelegate extends AnimationDelegate {
        private float mRange;
        private float mRadius;
        private float mDist;

        private ValueAnimator mAngleAnim;
        private float mVibrateAngle;

        LoadingAnimationDelegate(float cx, float cy, View parent, float radius, float dist, float range) {
            super(cx, cy, parent);
            this.mRadius = radius;
            this.mDist = dist;
            this.mRange = range;
        }

        @Override
        public void start() {
            stop();
            mAngleAnim = LauncherAnimUtils.ofFloat(mParent, 0f, 360f);
            mAngleAnim.setInterpolator(LauncherAnimUtils.LINEAR);
            mAngleAnim.setDuration(500);
            mAngleAnim.setRepeatCount(ValueAnimator.INFINITE);
            mAngleAnim.setRepeatMode(ValueAnimator.RESTART);
            mAngleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mVibrateAngle = (float) animation.getAnimatedValue();
                    mParent.invalidate();
                }
            });
            mAngleAnim.start();
        }

        @Override
        void postProgress(float progress) {
            throw new UnsupportedOperationException("Loading animation does not support manual progress control.");
        }

        @Override
        public void stop() {
            if (mAngleAnim != null && mAngleAnim.isRunning()) {
                mAngleAnim.cancel();
                mParent.invalidate();
            }
        }

        @Override
        public boolean isRunning() {
            if (mAngleAnim == null) {
                return false;
            }
            return mAngleAnim.isRunning();
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            if (mAngleAnim != null && mAngleAnim.isRunning()) {
                // Draw center circle
                paint.setColor(GREEN);
                float vibrateDelta = mCy + (float) Math.sin(Math.toRadians(mVibrateAngle + 90)) * mRange;
                canvas.drawCircle(mCx, vibrateDelta, mRadius, paint);

                // Draw left circle
                paint.setColor(YELLOW);
                vibrateDelta = mCy + (float) Math.sin(Math.toRadians(mVibrateAngle)) * mRange;
                canvas.drawCircle(mCx - mDist, vibrateDelta, mRadius, paint);

                // Draw right circle
                paint.setColor(BLUE);
                vibrateDelta = mCy + (float) Math.sin(Math.toRadians(mVibrateAngle + 180f)) * mRange;
                canvas.drawCircle(mCx + mDist, vibrateDelta, mRadius, paint);
            }
        }
    }
}


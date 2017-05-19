package com.ihs.inputmethod.feature.boost.animation;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.RotateAnimation;

public class DynamicRotateAnimation extends RotateAnimation {

    private static final int MULTIPLY_FACTOR = 100;
    private static final long ASSUMED_ROTATION_DEGREES = 360 * 60 * MULTIPLY_FACTOR; // 最大旋转角度

    private static final int ANIM_ACCELERATE = 0;
    private static final int ANIM_LINEAR = 1;
    private static final int ANIM_DECELERATE = 2;

    private static final long ASSUMED_TOTAL_DURATION = 1000 * 60 * MULTIPLY_FACTOR; // 最长持续时间 60 s
    private static final int ASSUMED_ACCELERATE_DURATION = 1600; // 加减速时长

    private ScanInterpolator mScanInterpolator;
    private int mAnimMode = ANIM_ACCELERATE;
    private boolean mHandleSucceeded;
    private float mRotatedAngle;

    public DynamicRotateAnimation(final float factor) {
        super(0, Float.valueOf(Math.signum(factor)).intValue() * ASSUMED_ROTATION_DEGREES,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        setDuration(ASSUMED_TOTAL_DURATION);
        mScanInterpolator = new ScanInterpolator(Math.abs(factor) * 100 * MULTIPLY_FACTOR, new InterpolatorListener() {
            @Override
            public void onCompleted(float currentDistance) {
                if (mHandleSucceeded) {
                    mRotatedAngle = Float.valueOf(Math.signum(factor)).intValue() * ASSUMED_ROTATION_DEGREES * currentDistance;
                    cancel();
                }
            }
        });
        setInterpolator(mScanInterpolator);
        setFillEnabled(true);
        setFillAfter(true);
        mAnimMode = ANIM_ACCELERATE;
    }

    public DynamicRotateAnimation() {
        this(1f);
    }

    public void start() {
        super.start();
        mAnimMode = ANIM_ACCELERATE;
    }

    public float getRotatedAngle() {
        return mRotatedAngle;
    }

    public void handleProcess(int currentIndex, int total) {
        if (currentIndex == total) {
            // 处理完毕时
            startDecelerateMode();
        }
    }

    public void startDecelerateMode() {
        mHandleSucceeded = true;
        mScanInterpolator.enableDecelerateMode();
    }

    interface InterpolatorListener {
        void onCompleted(float currentDistance);
    }

    private class ScanInterpolator extends AccelerateDecelerateInterpolator {
        // The acceleration
        private float mAcceleration = 100f;
        // 预估加速阶段所占总时长百分比
        private float mAssumeAccelerateDurationPercents = ASSUMED_ACCELERATE_DURATION / (float) ASSUMED_TOTAL_DURATION;
        // 匀速结束相对时间
        private float mLinearEndPercents = 0f;
        // 已经走过的路程
        private float mCurrentDistance = 0f;
        // 加速结束时走过的路程
        private float mAccelerateEndDistance = 0f;
        // 匀速结束时走过的路程
        private float mLinearEndDistance = 0f;
        // 最高速度
        private float mLinearSpeed = 0f;
        // 动画是否完成
        private boolean mIsFinished = false;
        // 动画开始的绝对时间
        private long mAnimationStartTime = System.currentTimeMillis();

        private InterpolatorListener mListener;

        public ScanInterpolator(float acceleration, InterpolatorListener listener) {
            super();
            this.mAcceleration = Math.abs(acceleration);
            mListener = listener;
        }

        public float getInterpolation(float currentPercent) {
            if (mAnimationStartTime == 0) {
                mAnimationStartTime = System.currentTimeMillis();
            }
            if (mAnimMode == ANIM_DECELERATE) {
                //Decelerate mode
                float decelerateDistance = mLinearSpeed * (currentPercent - mLinearEndPercents) -
                        mAcceleration * (currentPercent - mLinearEndPercents) * (currentPercent - mLinearEndPercents) / 2;
                if (mAcceleration * (currentPercent - mLinearEndPercents) < mLinearSpeed) {
                    mCurrentDistance = mLinearEndDistance + decelerateDistance;
                } else {
                    mIsFinished = true;
                    if (mListener != null) {
                        mListener.onCompleted(mCurrentDistance);
                    }
                }
            } else {
                if (currentPercent < mAssumeAccelerateDurationPercents) {
                    //Accelerate mode
                    mAnimMode = ANIM_ACCELERATE;
                    mCurrentDistance = mAcceleration * currentPercent * currentPercent / 2;
                    mAccelerateEndDistance = mCurrentDistance;
                    mLinearSpeed = mAcceleration * currentPercent;
                } else {
                    //Linear mode
                    mAnimMode = ANIM_LINEAR;
                    mCurrentDistance = mLinearSpeed * (currentPercent - mAssumeAccelerateDurationPercents) + mAccelerateEndDistance;
                }
                mLinearEndPercents = currentPercent;
                mLinearEndDistance = mCurrentDistance;
            }
            return mCurrentDistance;
        }

        public boolean isFinished() {
            return mIsFinished;
        }

        public void enableDecelerateMode() {
            mAnimMode = ANIM_DECELERATE;
        }
    }
}


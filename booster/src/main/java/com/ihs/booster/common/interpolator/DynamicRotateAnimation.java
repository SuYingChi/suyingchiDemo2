package com.ihs.booster.common.interpolator;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.RotateAnimation;

public class DynamicRotateAnimation extends RotateAnimation {
    private static int multipleFactor = 100;
    private static long assumeRotateDegree = 360 * 60 * multipleFactor; // 最大旋转角度
    private int ANIM_ACCELERATE = 0;
    private int ANIM_LINEAR = 1;
    private int ANIM_DECELERATE = 2;
    private long assumeTotalDuration = 1000 * 60 * multipleFactor;// 最长持续时间 60 s
    private int assumeAccelerateDuration = 1600; // 加减速时长 1.6 s
    private ScanInterpolator scanInterpolator;
    private volatile Integer animMode = ANIM_ACCELERATE;
    private volatile boolean handleSuccessed = false;
    private float rotatedAngle = 0f;

    public DynamicRotateAnimation(final float factor) {
        super(0, Float.valueOf(Math.signum(factor)).intValue() * assumeRotateDegree, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation
                .RELATIVE_TO_SELF, 0.5f);
        setDuration(assumeTotalDuration);
        scanInterpolator = new ScanInterpolator(Math.abs(factor) * 100 * multipleFactor, new InterpolatorListener() {
            @Override
            public void onCompleted(float currentDistance) {
                if (handleSuccessed) {
                    rotatedAngle = Float.valueOf(Math.signum(factor)).intValue() * assumeRotateDegree * currentDistance;
                    cancel();
                }
            }
        });
        setInterpolator(scanInterpolator);
        setFillEnabled(true);
        setFillAfter(true);
        animMode = ANIM_ACCELERATE;
    }

    public DynamicRotateAnimation() {
        this(1f);
    }

    public void start() {
        super.start();
        animMode = ANIM_ACCELERATE;
    }

    public float getRotatedAngle() {
        return rotatedAngle;
    }

    public void handleProcess(int currentIndex, int total) {
        if (currentIndex == total) {
            //处理完毕时
            startDecelerateMode();
        }
    }

    public void startDecelerateMode() {
        handleSuccessed = true;
        scanInterpolator.enableDecelerateMode();
    }

    interface InterpolatorListener {
        void onCompleted(float currentDistance);
    }

    private class ScanInterpolator extends AccelerateDecelerateInterpolator {
        //the acceleration
        private float a = 100f;
        //预估加速阶段所占总时长百分比
        private float assumeAccelerateDurationPencent = assumeAccelerateDuration / (float) assumeTotalDuration;
        //匀速结束相对时间
        private float linearEndPercent = 0f;
        //已经走过的路程
        private float currentDistance = 0f;
        //加速结束时走过的路程
        private float accelerateEndDistance = 0f;
        //匀速结束时走过的路程
        private float linearEndDistance = 0f;
        //最高时速
        private float linearSpeed = 0f;
        //动画是否完成
        private boolean isFinished = false;
        //动画开始的绝对时间
        private long animationStartTime = System.currentTimeMillis();
        private InterpolatorListener listener;

        public ScanInterpolator(float a, InterpolatorListener listener) {
            super();
            this.a = Math.abs(a);
            this.listener = listener;
        }

        public float getInterpolation(float currentPercent) {
            if (animationStartTime == 0) {
                animationStartTime = System.currentTimeMillis();
            }
            if (animMode == ANIM_DECELERATE) {
                //Decelerate mode
                float decelerateDistance = linearSpeed * (currentPercent - linearEndPercent) - a * (currentPercent -
                        linearEndPercent) * (currentPercent - linearEndPercent) / 2;
                if (a * (currentPercent - linearEndPercent) < linearSpeed) {
                    currentDistance = linearEndDistance + decelerateDistance;
                } else {
                    isFinished = true;
                    if (listener != null) {
                        listener.onCompleted(currentDistance);
                    }
                }
            } else {
                if (currentPercent < assumeAccelerateDurationPencent) {
                    //Accelerate mode
                    animMode = ANIM_ACCELERATE;
                    currentDistance = a * currentPercent * currentPercent / 2;
                    accelerateEndDistance = currentDistance;
                    linearSpeed = a * currentPercent;
                } else {
                    //Linear mode
                    animMode = ANIM_LINEAR;
                    currentDistance = linearSpeed * (currentPercent - assumeAccelerateDurationPencent) + accelerateEndDistance;
                }
                linearEndPercent = currentPercent;
                linearEndDistance = currentDistance;
            }
            return currentDistance;
        }

        public boolean isFinished() {
            return isFinished;
        }

        public void enableDecelerateMode() {
            animMode = ANIM_DECELERATE;
        }
    }
}


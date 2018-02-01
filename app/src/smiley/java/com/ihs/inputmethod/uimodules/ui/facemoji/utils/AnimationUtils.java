package com.ihs.inputmethod.uimodules.ui.facemoji.utils;


import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public final class AnimationUtils {


// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * create rotate animation
//     * @param duration
//     * @param ifRepeat
//     * @return
//     */
//    public static RotateAnimation getRotateAnimation(int duration, boolean ifRepeat){
//        return getRotateAnimation(0, 359, duration, ifRepeat);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
    public static RotateAnimation getRotateAnimation(int startDegree, int endDegree, int duration, boolean ifRepeat){
        RotateAnimation animation = new RotateAnimation(startDegree, endDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        if(ifRepeat) {
            animation.setRepeatCount(-1);
        }

        return animation;
    }


    public static Animation createPressAnimation() {
        return createScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, 10);
    }

    public static Animation createReleaseAnimation() {
        return createScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f, 10);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static Animation createLayerOneAnimation() {
//        final ScaleAnimation animation = new ScaleAnimation(0.0f, 4.0f, 0.0f, 4.0f,
//                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        animation.setDuration(200);//设置动画持续时间
//        animation.setFillAfter(true);
//        return animation;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static Animation createLayerTwoAnimation() {
//        return createFadeAnimation(1.0f, 0.0f, 500);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static Animation createLayerTwoTextFadeInAnimation() {
//        return createFadeAnimation(0.0f, 1.0f, 500);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static Animation createLayerTwoTextFadeOutAnimation() {
//        return createFadeAnimation(1.0f, 0.0f, 500);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static Animation createLayerThreeAnimation() {
//        return createFadeAnimation(0.0f, 1.0f, 500);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public static Animation createScaleAnimation(final float fromX, final float toX, final float fromY, final float toY, final int duration) {
        final ScaleAnimation animation = new ScaleAnimation(fromX, toX, fromY, toY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);//设置动画持续时间
        animation.setFillAfter(true);
        return animation;
    }

    public static Animation createFadeAnimation(final float fromAlpha, final float toAlpha, final int duration) {
        final AlphaAnimation animation = new AlphaAnimation(fromAlpha, toAlpha);
        animation.setDuration(duration);//设置动画持续时间
        return animation;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static Animation createTranslateAnimation(final float fromX,
//                                                     final float toX,
//                                                     final float fromY,
//                                                     final float toY,
//                                                     final int duration,
//                                                     final Interpolator interpolator) {
//        Animation animation = new TranslateAnimation(
//                Animation.ABSOLUTE, fromX,
//                Animation.ABSOLUTE, toX,
//                Animation.ABSOLUTE, fromY,
//                Animation.ABSOLUTE, toY);
//
//        animation.setDuration(duration);
//        animation.setInterpolator(interpolator);
//
//        return animation;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
}

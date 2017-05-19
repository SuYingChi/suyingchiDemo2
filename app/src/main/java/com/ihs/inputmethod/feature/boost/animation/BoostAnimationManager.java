package com.ihs.inputmethod.feature.boost.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;

import com.honeycomb.launcher.BuildConfig;
import com.honeycomb.launcher.compat.LauncherActivityInfoCompat;
import com.honeycomb.launcher.compat.LauncherAppsCompat;
import com.honeycomb.launcher.compat.UserHandleCompat;
import com.honeycomb.launcher.util.CommonUtils;
import com.honeycomb.launcher.util.Utils;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BoostAnimationManager {

    public static class Boost {
        public static final int ICON_ONE = 0;
        public static final int ICON_TWO = 1;
        public static final int ICON_THREE = 2;
        public static final int ICON_FOUR = 3;
        public static final int ICON_FIVE = 4;
        public static final int ICON_SIX = 5;
        public static final int ICON_SEVEN = 6;
    }

    private static final int FRAME = 68;
    private static final float ALPHA_REDUCE_MIN = 0.15f;
    private static final float WIDTH_SCREEN = CommonUtils.getPhoneWidth(HSApplication.getContext());
    private static final float HEIGHT_SCREEN = CommonUtils.getPhoneHeight(HSApplication.getContext());

    private static final float ROTATE_ANGLE_TOTAL = 360;

    private static final float WIDTH_SCREEN_TAG = 1080;
    private static final float HEIGHT_SCREEN_TAG = 1920;

    private static final float SCALE_START = 0.65f;
    public static final int COUNT_ICON = 7;

    // App icon 1
    private static final float[] X_ONE = new float[]{
            338.2f, 345.9f, 353.7f, 361.7f, 369.9f, 378.4f, 387.2f, 396.2f, 405.6f, 415.2f, 425.2f,
            435.6f, 446.3f, 457.3f, 468.8f, 480.6f, 492.9f, 505.5f, 518.6f, 532.0f, 545.9f, 560.2f,
            574.8f, 589.7f, 605.0f, 620.4f, 636.0f, 651.6f, 666.9f, 681.6f, 695.3f, 707.1f, 715.6f,
            718.4f, 710.8f, 683.5f, 627.1f, 551.0f, 474.5f, 453.9f, 539.9f
    };
    private static final float[] Y_ONE = new float[]{
            -96.1f, -84.1f, -71.8f, -59.1f, -45.9f, -32.3f, -18.2f, -3.5f, 11.8f, 27.8f, 44.4f,
            61.8f, 79.9f, 98.9f, 118.7f, 139.5f, 161.3f, 184.2f, 208.2f, 233.4f, 259.9f, 287.8f,
            317.1f, 348.1f, 380.9f, 415.6f, 452.3f, 491.3f, 532.8f, 577.1f, 624.6f, 675.4f, 730.1f,
            788.9f, 851.0f, 911.7f, 954.5f, 961.6f, 930.2f, 851.9f, 805.1f
    };
    private static final int TRANSLATE_START_DELAY_ONE = FRAME * 15;
    private static final int ALPHA_ZERO_DURATION_FRAME_ONE = 15;
    private static final int ALPHA_ADD_DURATION_FRAME_ONE = 7;

    // App icon 2
    private static final float[] X_TWO = new float[]{
            35.0f, 53.4f, 73.6f, 95.9f, 120.5f, 147.9f, 178.5f, 213.0f, 252.2f, 297.2f, 349.3f,
            410.5f, 482.4f, 565.6f, 652.1f, 707.5f, 656.4f, 539.9f
    };
    private static final float[] Y_TWO = new float[]{
            1073.2f, 1039.2f, 1003.6f, 966.3f, 927.3f, 886.7f, 844.6f, 802.1f, 756.9f, 712.6f,
            669.6f, 630.6f, 600.9f, 590.4f, 616.1f, 694.7f, 782.6f, 805.1f
    };
    private static final int TRANSLATE_START_DELAY_TWO = 0;
    private static final int ALPHA_ZERO_DURATION_FRAME_TWO = 0;
    private static final int ALPHA_ADD_DURATION_FRAME_TWO = 9;

    // App icon 3
    private static final float[] X_THREE = new float[]{
            -101.3f, -95.0f, -88.2f, -81.1f, -73.5f, -65.5f, -56.9f, -47.7f, -38.0f, -27.5f, -16.3f,
            -4.4f, 8.5f, 22.4f, 37.5f, 53.8f, 71.5f, 90.9f, 112.2f, 135.7f, 161.9f, 191.3f, 224.6f,
            263.0f, 307.9f, 361.7f, 428.5f, 513.2f, 611.0f, 682.7f, 664.4f, 539.9f
    };
    private static final float[] Y_THREE = new float[]{
            1731.0f, 1708.2f, 1684.3f, 1659.4f, 1633.3f, 1605.9f, 1577.3f, 1547.4f, 1516.1f, 1483.3f,
            1449.0f, 1413.0f, 1375.4f, 1336.0f, 1294.8f, 1251.6f, 1206.3f, 1159.0f, 1109.4f, 1057.6f,
            1003.4f, 947.0f, 888.4f, 827.9f, 766.4f, 705.7f, 650.4f, 613.1f, 624.4f, 702.8f, 810.7f,
            805.1f
    };
    private static final int TRANSLATE_START_DELAY_THREE = FRAME * 9;
    private static final int ALPHA_ZERO_DURATION_FRAME_THREE = 15;
    private static final int ALPHA_ADD_DURATION_FRAME_THREE = 7;

    // App icon 4
    private static final float[] X_FOUR = new float[]{
            533.8f, 529.9f, 525.9f, 521.8f, 517.6f, 513.2f, 508.7f, 504.1f, 499.3f, 494.3f, 489.2f,
            483.8f, 478.4f, 472.7f, 466.9f, 460.9f, 454.7f, 448.3f, 441.8f, 435.2f, 428.4f, 421.4f,
            414.4f, 407.3f, 400.2f, 393.2f, 386.2f, 379.4f, 373.0f, 367.0f, 361.8f, 357.6f, 354.9f,
            354.4f, 357.2f, 365.2f, 381.6f, 413.6f, 475.4f, 568.1f, 650.2f, 656.2f,
    };
    private static final float[] Y_FOUR = new float[]{
            2031.8f, 2017.4f, 2002.6f, 1987.3f, 1971.4f, 1954.8f, 1937.6f, 1919.7f, 1901.0f, 1881.4f,
            1861.0f, 1839.7f, 1817.3f, 1793.9f, 1769.5f, 1743.9f, 1717.0f, 1688.9f, 1659.4f, 1628.5f,
            1596.1f, 1562.1f, 1526.3f, 1488.8f, 1449.3f, 1407.8f, 1364.2f, 1318.1f, 1269.6f, 1218.3f,
            1164.2f, 1106.9f, 1046.2f, 981.9f, 913.7f, 841.5f, 766.0f, 690.0f, 628.1f, 622.0f, 682.0f,
            784.6f, 814.3f
    };
    private static final int TRANSLATE_START_DELAY_FOUR = FRAME * 7;
    private static final int ALPHA_ZERO_DURATION_FRAME_FOUR = 17;
    private static final int ALPHA_ADD_DURATION_FRAME_FOUR = 7;

    // App icon 5
    private static float[] X_FIVE = new float[]{
            1163.0f, 1154.8f, 1146.0f, 1136.7f, 1126.8f, 1116.4f, 1105.2f, 1093.4f, 1080.8f, 1067.3f,
            1053.0f, 1037.7f, 1021.3f, 1003.8f, 985.1f, 965.1f, 943.5f, 920.4f, 895.4f, 868.4f,
            839.2f, 807.5f, 772.7f, 734.5f, 692.1f, 644.5f, 590.3f, 527.2f, 457.0f, 410.1f, 405.5f,
            447.1f, 522.7f
    };
    private static float[] Y_FIVE = new float[]{
            430.7f, 440.9f, 451.8f, 463.2f, 475.2f, 487.9f, 501.4f, 515.6f, 530.6f, 546.5f, 563.2f,
            581.0f, 599.7f, 619.5f, 640.3f, 662.4f, 685.6f, 710.1f, 735.8f, 762.9f, 792.1f, 820.8f,
            851.6f, 883.3f, 915.5f, 947.1f, 975.9f, 995.9f, 988.1f, 928.5f, 844.1f, 763.2f, 815.2f
    };
    private static final int TRANSLATE_START_DELAY_FIVE = 0;
    private static final int ALPHA_ZERO_DURATION_FRAME_FIVE = 15;
    private static final int ALPHA_ADD_DURATION_FRAME_FIVE = 7;

    // App icon 6
    private static final float[] X_SIX = new float[]{
            1183.5f, 1176.2f, 1168.6f, 1160.7f, 1152.4f, 1143.7f, 1134.6f, 1125.1f, 1115.1f, 1104.6f,
            1093.5f, 1081.9f, 1069.7f, 1056.8f, 1043.2f, 1028.8f, 1013.6f, 997.5f, 980.4f, 962.2f,
            942.8f, 922.1f, 899.9f, 876.0f, 850.3f, 822.4f, 791.9f, 758.3f, 721.1f, 679.1f, 630.8f,
            573.8f, 504.2f, 427.2f, 380.6f, 378.4f, 448.4f, 539.9f
    };
    private static final float[] Y_SIX = new float[]{
            27.4f, 43.1f, 59.4f, 76.4f, 94.0f, 112.3f, 131.3f, 151.1f, 171.7f, 193.2f, 215.6f,
            238.9f, 263.1f, 288.4f, 314.8f, 342.3f, 371.0f, 400.9f, 432.0f, 464.5f, 498.4f, 533.8f,
            570.6f, 609.0f, 648.9f, 690.5f, 733.5f, 778.0f, 823.7f, 869.9f, 915.3f, 956.8f, 984.4f,
            968.9f, 897.6f, 804.9f, 745.0f, 805.1f
    };
    private static final int TRANSLATE_START_DELAY_SIX = FRAME * 18;
    private static final int ALPHA_ZERO_DURATION_FRAME_SIX = 12;
    private static final int ALPHA_ADD_DURATION_FRAME_SIX = 7;

    // App icon 7
    private static final float[] X_SEVEN = new float[]{
            755.0f, 756.0f, 757.0f, 758.0f, 759.0f, 759.9f, 760.8f, 761.7f, 762.5f, 763.3f, 764.0f,
            764.6f, 765.0f, 765.3f, 765.5f, 765.4f, 765.0f, 764.3f, 763.2f, 761.7f, 759.5f, 756.5f,
            752.6f, 747.4f, 740.6f, 731.7f, 719.7f, 703.3f, 680.2f, 645.6f, 591.8f, 521.9f, 459.5f,
            417.7f, 443.2f, 539.9f
    };
    private static final float[] Y_SEVEN = new float[]{
            11.3f, 28.0f, 45.3f, 63.3f, 82.0f, 101.4f, 121.7f, 142.8f, 164.8f, 187.8f, 211.7f,
            236.8f, 262.9f, 290.2f, 318.8f, 348.7f, 380.0f, 412.8f, 447.2f, 483.2f, 521.1f, 560.8f,
            602.5f, 646.3f, 692.4f, 740.8f, 791.4f, 844.1f, 897.9f, 949.7f, 987.9f, 986.1f, 942.2f,
            871.2f, 794.3f, 805.1f
    };
    private static final int TRANSLATE_START_DELAY_SEVEN = FRAME * 10;
    private static final int ALPHA_ZERO_DURATION_FRAME_SEVEN = 9;
    private static final int ALPHA_ADD_DURATION_FRAME_SEVEN = 7;

    static float[][] X_ICONS = {X_ONE, X_TWO, X_THREE, X_FOUR, X_FIVE, X_SIX, X_SEVEN};
    static float[][] Y_ICONS = {Y_ONE, Y_TWO, Y_THREE, Y_FOUR, Y_FIVE, Y_SIX, Y_SEVEN};
    static int[] TRANSLATE_START_DELAY_ICONS = {
            TRANSLATE_START_DELAY_ONE,
            TRANSLATE_START_DELAY_TWO,
            TRANSLATE_START_DELAY_THREE,
            TRANSLATE_START_DELAY_FOUR,
            TRANSLATE_START_DELAY_FIVE,
            TRANSLATE_START_DELAY_SIX,
            TRANSLATE_START_DELAY_SEVEN
    };

    private float endX;
    private float endY;
    private boolean isStartImmediately;

    private List<String> drawablePackageList = new ArrayList<>();
    private List<String> boostDrawablePackageList = new ArrayList<>();

    public BoostAnimationManager(float endX, float endY) {
        this.endX = endX;
        this.endY = endY;
    }

    public BoostAnimationManager(float endX, float endY, boolean isStartImmediately) {
        this.endX = endX;
        this.endY = endY;
        this.isStartImmediately = isStartImmediately;
    }

    public void startIconAnimation(final View view, int type) {
        int left = view.getLeft();
        int top = view.getTop();

        float startX = (float) left;
        float startY = (float) top;
        float[] xLocation;
        float[] yLocation;
        float[] translationX;
        float[] translationY;
        int alphaZeroFrames;
        int alphaAddFrames;
        int alphaReduceFrames;
        long startDelay;

        switch (type) {
            case Boost.ICON_ONE:
                xLocation = convertX(X_ONE);
                yLocation = convertY(Y_ONE);
                alphaZeroFrames = ALPHA_ZERO_DURATION_FRAME_ONE;
                alphaAddFrames = ALPHA_ADD_DURATION_FRAME_ONE;
                alphaReduceFrames = xLocation.length - alphaZeroFrames - alphaAddFrames;
                startDelay = isStartImmediately ? 0 : TRANSLATE_START_DELAY_ONE;
                translationX = getTranslationX(xLocation);
                translationY = getTranslationY(yLocation);
                break;
            case Boost.ICON_TWO:
                xLocation = convertX(X_TWO);
                yLocation = convertY(Y_TWO);
                alphaZeroFrames = ALPHA_ZERO_DURATION_FRAME_TWO;
                alphaAddFrames = ALPHA_ADD_DURATION_FRAME_TWO;
                alphaReduceFrames = xLocation.length - alphaAddFrames;
                startDelay = 0;
                translationX = getTranslationX(xLocation);
                translationY = getTranslationY(yLocation);
                break;
            case Boost.ICON_THREE:
                xLocation = convertX(X_THREE);
                yLocation = convertY(Y_THREE);
                alphaZeroFrames = ALPHA_ZERO_DURATION_FRAME_THREE;
                alphaAddFrames = ALPHA_ADD_DURATION_FRAME_THREE;
                alphaReduceFrames = xLocation.length - alphaAddFrames;
                startDelay = isStartImmediately ? 0 : TRANSLATE_START_DELAY_THREE;
                translationX = getTranslationX(xLocation);
                translationY = getTranslationY(yLocation);
                break;
            case Boost.ICON_FOUR:
                xLocation = convertX(X_FOUR);
                yLocation = convertY(Y_FOUR);
                alphaZeroFrames = ALPHA_ZERO_DURATION_FRAME_FOUR;
                alphaAddFrames = ALPHA_ADD_DURATION_FRAME_FOUR;
                alphaReduceFrames = xLocation.length - alphaAddFrames;
                startDelay = isStartImmediately ? 0 : TRANSLATE_START_DELAY_FOUR;
                translationX = getTranslationX(xLocation);
                translationY = getTranslationY(yLocation);
                break;
            case Boost.ICON_FIVE:
                xLocation = convertX(X_FIVE);
                yLocation = convertY(Y_FIVE);
                alphaZeroFrames = ALPHA_ZERO_DURATION_FRAME_FIVE;
                alphaAddFrames = ALPHA_ADD_DURATION_FRAME_FIVE;
                alphaReduceFrames = xLocation.length - alphaAddFrames;
                startDelay = isStartImmediately ? 0 : TRANSLATE_START_DELAY_FIVE;
                translationX = getTranslationX(xLocation);
                translationY = getTranslationY(yLocation);
                break;
            case Boost.ICON_SIX:
                xLocation = convertX(X_SIX);
                yLocation = convertY(Y_SIX);
                alphaZeroFrames = ALPHA_ZERO_DURATION_FRAME_SIX;
                alphaAddFrames = ALPHA_ADD_DURATION_FRAME_SIX;
                alphaReduceFrames = xLocation.length - alphaAddFrames;
                startDelay = isStartImmediately ? 0 : TRANSLATE_START_DELAY_SIX;
                translationX = getTranslationX(xLocation);
                translationY = getTranslationY(yLocation);
                break;
            case Boost.ICON_SEVEN:
                xLocation = convertX(X_SEVEN);
                yLocation = convertY(Y_SEVEN);
                alphaZeroFrames = ALPHA_ZERO_DURATION_FRAME_SEVEN;
                alphaAddFrames = ALPHA_ADD_DURATION_FRAME_SEVEN;
                alphaReduceFrames = xLocation.length - alphaAddFrames;
                startDelay = isStartImmediately ? 0 : TRANSLATE_START_DELAY_SEVEN;
                translationX = getTranslationX(xLocation);
                translationY = getTranslationY(yLocation);
                break;
            default:
                xLocation = convertX(X_ONE);
                yLocation = convertY(Y_ONE);
                alphaZeroFrames = ALPHA_ZERO_DURATION_FRAME_ONE;
                alphaAddFrames = ALPHA_ADD_DURATION_FRAME_ONE;
                alphaReduceFrames = xLocation.length - alphaAddFrames;
                startDelay = isStartImmediately ? 0 : TRANSLATE_START_DELAY_ONE;
                translationX = getTranslationX(xLocation);
                translationY = getTranslationY(yLocation);
                break;

        }

        int count = xLocation.length;

        if (endX != 0 && endY != 0) {
            float xOffset = endX - (startX + translationX[translationX.length - 1]);
            for (int i = 0; i < translationX.length; i++) {
                translationX[i] += xOffset;
            }

            float yOffset = endY - (startY + translationY[translationY.length - 1]);
            for (int i = 0; i < translationY.length; i++) {
                translationY[i] += yOffset;
            }
        }

        Keyframe[] keyframesX = new Keyframe[count];
        Keyframe[] keyframesY = new Keyframe[count];
        Keyframe[] keyframesScale = new Keyframe[count];
        Keyframe[] keyframesAlpha = new Keyframe[count];
        Keyframe[] keyframesRotate = new Keyframe[count];

        final float keyStep = 1f / (float) count;
        float key = keyStep;
        for (int i = 0; i < count; ++i) {
            keyframesX[i] = Keyframe.ofFloat(key, translationX[i]);
            keyframesY[i] = Keyframe.ofFloat(key, translationY[i]);

            float inputScale = (i + 1) / (float) count;
            float scale = getScaleReduce(inputScale) * SCALE_START;
            keyframesScale[i] = Keyframe.ofFloat(key, scale);

            if (i < alphaZeroFrames) {
                keyframesAlpha[i] = Keyframe.ofFloat(key, 0);
            } else if (i < alphaZeroFrames + alphaAddFrames) {
                float inputAlphaAdd = (i - alphaZeroFrames + 1) / (float) alphaAddFrames;
                float alpha = getAlphaAdd(inputAlphaAdd);
                keyframesAlpha[i] = Keyframe.ofFloat(key, alpha);
            } else if (i < alphaZeroFrames + alphaAddFrames + alphaReduceFrames) {
                float inputAlphaReduce = (i - alphaZeroFrames - alphaAddFrames + 1) / (float) alphaReduceFrames;
                float alpha = getAlphaReduce(inputAlphaReduce);
                keyframesAlpha[i] = Keyframe.ofFloat(key, alpha);
            } else {
                keyframesAlpha[i] = Keyframe.ofFloat(key, ALPHA_REDUCE_MIN);
            }

            float inputRotate = (i + 1) / (float) count;
            float rotateAngle = getRotateAdd(inputRotate) * ROTATE_ANGLE_TOTAL;
            keyframesRotate[i] = Keyframe.ofFloat(key, rotateAngle);
            key += keyStep;
        }
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofKeyframe("translationX", keyframesX);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofKeyframe("translationY", keyframesY);
        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofKeyframe("scaleX", keyframesScale);
        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofKeyframe("scaleY", keyframesScale);
        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofKeyframe("alpha", keyframesAlpha);
        PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", keyframesRotate);

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, pvhY, pvhX, pvhScaleX, pvhScaleY, pvhAlpha, pvhRotation).setDuration(FRAME * count);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        if (0 != startDelay) {
            objectAnimator.setStartDelay(startDelay);
        }

        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setVisibility(View.VISIBLE);
            }
        });
        objectAnimator.start();
    }

    /**
     * AccelerateDecelerateInterpolator
     *
     * @param input 0-1
     * @return 0-1
     */
    private float getAlphaAdd(float input) {
        return (float) (Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }

    /**
     * Parabola
     *
     * @param input 0-1
     * @return 0-1
     */
    private float getAlphaReduce(float input) {
        float[][] points = {{0, 1f}, {1f, ALPHA_REDUCE_MIN}, {0.5f, 0.75f}};
        float[] abc = calculateParabola(points);
        float a = abc[0];
        float b = abc[1];
        float c = abc[2];
        return a * input * input + b * input + c;
    }

    /**
     * Parabola
     *
     * @param input 0-1
     * @return 1-0
     */
    private float getScaleReduce(float input) {
        float[][] points = {{0, 1f}, {1f, 0}, {0.6f, 0.9f}};
        float[] abc = calculateParabola(points);
        float a = abc[0];
        float b = abc[1];
        float c = abc[2];
        return a * input * input + b * input + c;
    }

    /**
     * AccelerateInterpolator
     *
     * @param input 0-1
     * @return 0-1
     */
    private float getRotateAdd(float input) {
        return input * input;
    }

    private float[] convertX(float[] x) {
        float[] location = new float[x.length];
        for (int i = 0; i < x.length; i++) {
            location[i] = x[i] * WIDTH_SCREEN / WIDTH_SCREEN_TAG;
        }
        return location;
    }

    private float[] convertY(float[] y) {
        float[] location = new float[y.length];
        for (int i = 0; i < y.length; i++) {
            location[i] = y[i] * HEIGHT_SCREEN / HEIGHT_SCREEN_TAG;
        }
        return location;
    }

    private float[] getTranslationX(float[] location) {
        float[] translation = new float[location.length];
        for (int i = 0; i < location.length; i++) {
            if (i == 0) {
                translation[i] = 0;
            } else {
                translation[i] = (location[i] - location[0]) / WIDTH_SCREEN_TAG * WIDTH_SCREEN;
            }
        }
        return translation;
    }

    private float[] getTranslationY(float[] location) {
        float[] translation = new float[location.length];
        for (int i = 0; i < location.length; i++) {
            if (i == 0) {
                translation[i] = 0;
            } else {
                translation[i] = (location[i] - location[0]) / HEIGHT_SCREEN_TAG * HEIGHT_SCREEN;
            }
        }
        return translation;
    }

    Bitmap[] getBoostAppIconBitmaps(Context context) {
        Drawable[] drawables = getBoostAppIconDrawables(context);
        Bitmap[] bitmaps = new Bitmap[COUNT_ICON];
        for (int i = 0; i < drawables.length; i++) {
            bitmaps[i] = drawableToBitmap(drawables[i]);
        }
        return bitmaps;
    }

    public @NonNull
    Drawable[] getBoostAppIconDrawables(Context context) {
        drawablePackageList.clear();
        boostDrawablePackageList.clear();
        Drawable[] drawables = new Drawable[COUNT_ICON];
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return getRandomAppIcon(context, drawables, 0);
        }

        int i = 0;
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : runningAppProcesses) {
            if (null != appProcessInfo) {
                String processName = appProcessInfo.processName;
                if (!TextUtils.isEmpty(processName)) {
                    String packageName = processName.split(":")[0].trim();
                    if (packageName.equals(BuildConfig.APPLICATION_ID)) {
                        continue;
                    }
                    String securityPackageName = context.getPackageName();
                    boolean isSystemApp = isSystemApp(context, packageName);
                    boolean isLaunchAbleApp = isLaunchAbleApp(context, packageName);
                    boolean isSelf = false;
                    boolean isDuplicate = drawablePackageList.contains(packageName);
                    if (!TextUtils.isEmpty(securityPackageName)) {
                        isSelf = securityPackageName.equals(packageName);
                    }

                    if (!TextUtils.isEmpty(packageName) && !isSystemApp && isLaunchAbleApp && !isSelf && !isDuplicate) {
                        if (i >= drawables.length) {
                            break;
                        }
                        Drawable currentDrawable = Utils.getAppIcon(packageName);
                        drawables[i] = currentDrawable;
                        drawablePackageList.add(packageName);
                        boostDrawablePackageList.add(packageName);
                        i++;
                    }
                }
            }
        }

        if (i < drawables.length) {
            return getRandomAppIcon(context, drawables, i);
        }
        return drawables;
    }

    public List<String> getBoostDrawablePackageList(Context context) {
        getBoostAppIconDrawables(context);
        return boostDrawablePackageList;
    }

    private boolean isLaunchAbleApp(Context context, String packageName) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);
        return null != context.getPackageManager().resolveActivity(intent, 0);
    }

    private @NonNull
    Drawable[] getRandomAppIcon(Context context, Drawable[] drawables, int currentIndex) {
        if (null == drawables) {
            return new Drawable[0];
        }

        Collection<String> applicationInfoList = getAllAppPackageNames(context);
        if (null == applicationInfoList || applicationInfoList.size() == 0) {
            return new Drawable[0];
        }

        List<String> apps = new ArrayList<>();
        if (drawablePackageList.size() > 0) {
            for (String applicationInfo : applicationInfoList) {
                if (null != applicationInfo && !drawablePackageList.contains(applicationInfo)) {
                    apps.add(applicationInfo);
                }
            }
        } else {
            apps.addAll(applicationInfoList);
        }

        int size = apps.size();
        if (apps.size() == 0 || size < drawables.length) {
            return new Drawable[0];
        }

        if (currentIndex >= drawables.length) {
            return new Drawable[0];
        }

        int[] randomIndex = Utils.getUniqueRandomInts(0, size, drawables.length - currentIndex);
        for (int i = currentIndex; i < drawables.length; i++) {
            if (null != randomIndex && (i - currentIndex) < randomIndex.length) {
                int index = randomIndex[i - currentIndex];
                String packageName = apps.get(index);
                drawables[i] = Utils.getAppIcon(apps.get(index));
                boostDrawablePackageList.add(packageName);
            }
        }
        return drawables;
    }

    private static boolean isSystemApp(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            return null != applicationInfo && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    static void startSetAnimation(final View v, Animation... animations) {
        if (null == v) {
            return;
        }

        final AnimationSet animationSet = new AnimationSet(false);
        for (Animation animation : animations) {
            animationSet.addAnimation(animation);
        }
        v.startAnimation(animationSet);
    }

    static Animation getAlphaAppearAnimation(long duration, long startOffSet) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
        alphaAnimation.setDuration(duration);
        if (startOffSet != 0) {
            alphaAnimation.setStartOffset(startOffSet);
        }
        return alphaAnimation;
    }

    static Animation getBoostRotateAnimation(long duration) {
        final RotateAnimation animation = new RotateAnimation(0f, 3600, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        animation.setRepeatCount(0);
        AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
        animation.setInterpolator(accelerateDecelerateInterpolator);
        return animation;
    }

    static Animation getRotateAnimation(long duration, int toDegrees) {
        final RotateAnimation animation = new RotateAnimation(0f, toDegrees, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        animation.setRepeatCount(0);
        AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
        animation.setInterpolator(accelerateDecelerateInterpolator);
        return animation;
    }

    private static float[] calculateParabola(float[][] points) {
        float x1 = points[0][0];
        float y1 = points[0][1];
        float x2 = points[1][0];
        float y2 = points[1][1];
        float x3 = points[2][0];
        float y3 = points[2][1];

        final float a = (y1 * (x2 - x3) + y2 * (x3 - x1) + y3 * (x1 - x2))
                / (x1 * x1 * (x2 - x3) + x2 * x2 * (x3 - x1) + x3 * x3 * (x1 - x2));
        final float b = (y1 - y2) / (x1 - x2) - a * (x1 + x2);
        final float c = y1 - (x1 * x1) * a - x1 * b;
        return new float[]{a, b, c};
    }

    private Collection<String> getAllAppPackageNames(Context context) {
        Set<String> packageNames = new HashSet<>();
        UserHandleCompat user = UserHandleCompat.myUserHandle();
        List<LauncherActivityInfoCompat> launcherAppInfos =
                LauncherAppsCompat.getInstance(context).getActivityList(null, user);
        for (LauncherActivityInfoCompat launcherAppsInfo : launcherAppInfos) {
            if (launcherAppsInfo.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                continue;
            }
            packageNames.add(launcherAppsInfo.getApplicationInfo().packageName);
        }
        return packageNames;
    }

    private static @NonNull
    Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return Utils.createFallbackBitmap();
        }
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Utils.createFallbackBitmap(); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}

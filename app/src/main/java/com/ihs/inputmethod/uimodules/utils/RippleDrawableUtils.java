package com.ihs.inputmethod.uimodules.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;

import java.util.Arrays;

/**
 * Created by jixiang on 16/12/22.
 */

public class RippleDrawableUtils {

    private static final float DEFAULT_RIPPLE_COLOR_LEVEL = 0.8f;

    /**
     * for general button with ripple above 5.0 and selector lower than.
     *
     * @param normalColorID id for normal color
     * @return backgroundDrawable
     */
    public static Drawable getButtonRippleBackground(
            int normalColorID) {

        float radius = HSDisplayUtils.dip2px(2);
        int normalColor = HSApplication.getContext().getResources().getColor(normalColorID);
        return getCompatRippleDrawable(normalColor, getRippleColor(normalColor, DEFAULT_RIPPLE_COLOR_LEVEL), radius);
    }

    public static Drawable getTransparentRippleBackground() {

        float radius = 0;
        int normalColor = Color.TRANSPARENT;
        int pressedColor = Color.parseColor("#30ffffff");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return HSApplication.getContext().getResources().getDrawable(R.drawable.base_function_bg);
        } else {
            return getStateListDrawable(normalColor, pressedColor, -1, radius);
        }
    }

    public static Drawable getCompatRippleDrawable(
            int normalColor, int pressedColor, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(getRippleColor(normalColor, DEFAULT_RIPPLE_COLOR_LEVEL)),
                    getStateListDrawable(normalColor, pressedColor, -1, radius), getRippleMask(normalColor, radius));
        } else {
            return getStateListDrawable(normalColor, pressedColor, -1, radius);
        }
    }

    public static Drawable getCompatRippleDrawable(
            int normalColor, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(getRippleColor(normalColor, DEFAULT_RIPPLE_COLOR_LEVEL)),
                    getStateListDrawable(normalColor, -1, -1, radius), getRippleMask(normalColor, radius));
        } else {
            return getStateListDrawable(normalColor, getRippleColor(normalColor, DEFAULT_RIPPLE_COLOR_LEVEL), -1, radius);
        }
    }

    public static Drawable getCompatCircleRippleDrawable(
            int normalColor, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(getRippleColor(normalColor, DEFAULT_RIPPLE_COLOR_LEVEL)),
                    getCircleStateListDrawable(normalColor, -1, -1, radius), getCircleRippleMask(normalColor, radius));
        } else {
            return getCircleStateListDrawable(normalColor, getRippleColor(normalColor, DEFAULT_RIPPLE_COLOR_LEVEL), -1, radius);
        }
    }

    public static Drawable getContainDisableStatusCompatRippleDrawable(int normalColor, int disableColor, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(getRippleColor(normalColor, 0.5f))
                    , getStateListDrawable(normalColor, -1, disableColor, radius)
                    , getRippleMask(normalColor, radius));
        } else {
            return getStateListDrawable(normalColor, getRippleColor(normalColor, 0.5f), disableColor, radius);
        }
    }

    public static Drawable getCompatGradientRippleDrawableContainDisableStatus(int startColor, int endColor, int disableColor, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(getRippleColor(startColor, 0.5f))
                    , getStateListDrawable(startColor, endColor, -1, disableColor, radius)
                    , getRippleMask(startColor, radius));
        } else {
            return getStateListDrawable(startColor, endColor, getRippleColor(startColor, 0.5f), disableColor, radius);
        }
    }

    private static int getRippleColor(int normalColor, float level) {
        int r = (int) (((normalColor >> 16) & 0xFF) * level);
        int g = (int) (((normalColor >> 8) & 0xFF) * level);
        int b = (int) (((normalColor) & 0xFF) * level);
        return Color.rgb(r, g, b);
    }

    private static Drawable getRippleMask(int color, float radius) {
        float[] outerRadii = new float[8];
        Arrays.fill(outerRadii, radius);

        RoundRectShape r = new RoundRectShape(outerRadii, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(r);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    private static Drawable getCircleRippleMask(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(color);
        return shape;
    }

    public static StateListDrawable getStateListDrawable(
            int normalColor, int pressedColor, int disableColor, float radius) {
        StateListDrawable states = new StateListDrawable();
        if (disableColor != -1) {
            states.addState(new int[]{-android.R.attr.state_enabled},
                    getShapeDrawable(disableColor, radius));
        }
        if (pressedColor != -1) {
            states.addState(new int[]{android.R.attr.state_pressed},
                    getShapeDrawable(pressedColor, radius));
            states.addState(new int[]{android.R.attr.state_focused},
                    getShapeDrawable(pressedColor, radius));
            states.addState(new int[]{android.R.attr.state_activated},
                    getShapeDrawable(pressedColor, radius));
        }
        states.addState(new int[]{},
                getShapeDrawable(normalColor, radius));
        return states;
    }

    public static StateListDrawable getStateListDrawable(
            int startColor, int endColor, int pressedColor, int disableColor, float radius) {
        StateListDrawable states = new StateListDrawable();
        if (disableColor != -1) {
            states.addState(new int[]{-android.R.attr.state_enabled},
                    getShapeDrawable(disableColor, radius));
        }
        if (pressedColor != -1) {
            states.addState(new int[]{android.R.attr.state_pressed},
                    getShapeDrawable(pressedColor, radius));
            states.addState(new int[]{android.R.attr.state_focused},
                    getShapeDrawable(pressedColor, radius));
            states.addState(new int[]{android.R.attr.state_activated},
                    getShapeDrawable(pressedColor, radius));
        }
        states.addState(new int[]{},
                getShapeDrawable(startColor, endColor, radius));
        return states;
    }

    private static StateListDrawable getCircleStateListDrawable(
            int normalColor, int pressedColor, int disableColor, float radius) {
        StateListDrawable states = new StateListDrawable();
        if (disableColor != -1) {
            states.addState(new int[]{-android.R.attr.state_enabled},
                    getCircleShapeDrawable(disableColor, radius));
        }
        if (pressedColor != -1) {
            states.addState(new int[]{android.R.attr.state_pressed},
                    getCircleShapeDrawable(pressedColor, radius));
            states.addState(new int[]{android.R.attr.state_focused},
                    getCircleShapeDrawable(pressedColor, radius));
            states.addState(new int[]{android.R.attr.state_activated},
                    getCircleShapeDrawable(pressedColor, radius));
        }
        states.addState(new int[]{},
                getCircleShapeDrawable(normalColor, radius));
        return states;
    }

    public static GradientDrawable getShapeDrawable(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(radius);
        shape.setColor(color);
        return shape;
    }


    private static GradientDrawable getShapeDrawable(int startColor, int endColor, float radius) {
        int[] colors = {startColor, endColor};
        GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        shape.setCornerRadius(radius);
        return shape;
    }

    private static GradientDrawable getCircleShapeDrawable(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(color);
        return shape;
    }

}

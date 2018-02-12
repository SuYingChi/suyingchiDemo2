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
    private static GradientDrawable getCircleShapeDrawable(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(color);
        return shape;
    }
    public static Drawable getCompatRippleDrawable(
            int normalColor, int pressedColor, int normalStrokeColor, int pressedStrokeColor, int strokeWidth, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(getRippleColor(normalColor)),
                    getStateListDrawable(normalColor, pressedColor, -1, normalStrokeColor, pressedStrokeColor, -1, strokeWidth, radius), getRippleMask(getRippleMaskColor(pressedColor), radius));
        } else {
            return getStateListDrawable(normalColor, pressedColor, -1, normalStrokeColor, pressedStrokeColor, -1, strokeWidth, radius);
        }
    }

    private static int getRippleMaskColor(int normalColor) {
        int alpha = Color.alpha(normalColor);
        int rippleMaskColor = normalColor;
        if (alpha < 255) { //如果默认值有透明度，则ripple值设置为默认
            rippleMaskColor = 0xfffea50b;
        }
        return rippleMaskColor;
    }


    private static int getRippleColor(int normalColor) {
        int r = (int) (((normalColor >> 16) & 0xFF) * 0.8);
        int g = (int) (((normalColor >> 8) & 0xFF) * 0.8);
        int b = (int) (((normalColor) & 0xFF) * 0.8);
        return Color.rgb(r, g, b);
    }

    public static StateListDrawable getStateListDrawable(
            int normalColor, int pressedColor, int disableColor, int normalStrokeColor, int pressedStrokeColor, int disableStrokeColor, int strokeWidth, float radius) {
        StateListDrawable states = new StateListDrawable();
        if (disableColor != -1) {
            states.addState(new int[]{-android.R.attr.state_enabled},
                    getShapeDrawable(disableColor, disableStrokeColor, strokeWidth, radius));
        }
        if (pressedColor != -1) {
            states.addState(new int[]{android.R.attr.state_pressed},
                    getShapeDrawable(pressedColor, pressedStrokeColor, strokeWidth, radius));
            states.addState(new int[]{android.R.attr.state_focused},
                    getShapeDrawable(pressedColor, pressedStrokeColor, strokeWidth, radius));
            states.addState(new int[]{android.R.attr.state_activated},
                    getShapeDrawable(pressedColor, pressedStrokeColor, strokeWidth, radius));
        }
        states.addState(new int[]{},
                getShapeDrawable(normalColor, normalStrokeColor, strokeWidth, radius));
        return states;
    }

    public static GradientDrawable getShapeDrawable(int color, int strokeColor, int strokeWidth, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(radius);
        shape.setStroke(strokeWidth, strokeColor);
        shape.setColor(color);
        return shape;
    }


    public static Drawable getTransparentButtonBackgroundDrawable(int frameColor, int radius) {
        return getCompatRippleDrawable(Color.TRANSPARENT, 0x1a000000, frameColor, frameColor, HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.transparent_button_frame_stoke_width), radius);
    }

    public static Drawable getHalfTransparentButtonBackgroundDrawable(int radius) {
        return getCompatRippleDrawable(0x4c000000, 0x7f000000, radius);
    }
}

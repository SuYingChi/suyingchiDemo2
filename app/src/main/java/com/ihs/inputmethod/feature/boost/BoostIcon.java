package com.ihs.inputmethod.feature.boost;

import android.support.annotation.ColorInt;

public class BoostIcon {

    public static final int RED_PERCENTAGE = 80;
    public static final int GREEN_PERCENTAGE = 55;

    @ColorInt
    public static final int RED_COLOR = 0xfff32222;
    @ColorInt
    public static final int ORANGE_COLOR = 0xfffb9400;
    @ColorInt
    public static final int GREEN_COLOR = 0xff00c176;

    /**
     * @param percentage 0 - 100
     */
    @ColorInt
    public static int getProgressColor(int percentage) {
        if (percentage <= GREEN_PERCENTAGE) {
            return GREEN_COLOR;
        } else if (percentage < RED_PERCENTAGE) {
            return ORANGE_COLOR;
        } else {
            return RED_COLOR;
        }
    }
}

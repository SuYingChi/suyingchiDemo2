package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.ads;

/**
 * Created by chenyuanming on 29/03/2017.
 */

public class AdsItem {
    public float widthRatio;
    public float heightRatio;
    public boolean isCircleStyle;

    public AdsItem(boolean isCircleStyle) {
        this(1, 1, isCircleStyle);
    }

    public AdsItem(float widthRatio, float heightRatio, boolean isCircleStyle) {
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
        this.isCircleStyle = isCircleStyle;
    }
}

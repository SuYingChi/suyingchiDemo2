package com.ihs.inputmethod.uimodules.ui.facemoji.bean;

/**
 * Created by xu.zhang on 2/26/16.
 */
public class FacePictureParam {
    public final int width;
    public final int height;
    public final float translateX;
    public final float translateY;
    public final float scaleX;
    public final float scaleY;
    public final float skewX;
    public final float skewY;

    public FacePictureParam(int w, int h, float transX, float transY, float sclX, float sclY, float skX, float skY){
        width = w;
        height =h;
        translateX = transX;
        translateY = transY;
        scaleX = sclX;
        scaleY = sclY;
        skewX = skX;
        skewY = skY;
    }

    @Override
    public String toString() {
        return "FacePictureParam{" +
                "width=" + width +
                ", height=" + height +
                ", translateX=" + translateX +
                ", translateY=" + translateY +
                ", scaleX=" + scaleX +
                ", scaleY=" + scaleY +
                ", skewX=" + skewX +
                ", skewY=" + skewY +
                '}';
    }
}

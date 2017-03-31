package com.ihs.inputmethod.uimodules.mediacontroller.converts.gif2bitmap;

import android.graphics.Bitmap;

/**
 * Created by ihandysoft on 16/7/27.
 */
public class GifFrame {
    public Bitmap image;
    public int delay;
    public GifFrame nextFrame = null;

    public GifFrame(Bitmap im, int del) {
        image = im;
        delay = del;
    }

}

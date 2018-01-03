package com.ihs.inputmethod.uimodules.widget.videoview;

import android.graphics.drawable.Drawable;

/**
 * Created by ihandysoft on 16/12/29.
 */

public interface IMediaView {

    public void setHSBackground(Drawable drawable);

    public void setHSBackground(final String[] filePath);

    public void setHSBackground(int resId);

    public boolean isMedia();

    public void stopHSMedia();
}


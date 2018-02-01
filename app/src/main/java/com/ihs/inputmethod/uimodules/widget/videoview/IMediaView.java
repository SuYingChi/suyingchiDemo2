package com.ihs.inputmethod.uimodules.widget.videoview;

import android.graphics.drawable.Drawable;

/**
 * Created by ihandysoft on 16/12/29.
 */

public interface IMediaView {

    void setHSBackground(Drawable drawable);

    void setHSBackground(final String[] filePath);

    boolean isMedia();

    void stopHSMedia();
}


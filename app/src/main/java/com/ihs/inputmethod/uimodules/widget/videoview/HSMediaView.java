package com.ihs.inputmethod.uimodules.widget.videoview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by ihandysoft on 17/1/9.
 */

public class HSMediaView extends FrameLayout implements IMediaView {

    private IMediaView mediaView;

    private boolean supportSmoothScroll;

    private float radius;

    boolean isInitialized;

    public HSMediaView(Context context) {
        this(context, null);
    }

    public HSMediaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSMediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MediaView);
            radius = ta.getDimension(R.styleable.MediaView_backgroundRadius, 0);
            supportSmoothScroll = ta.getBoolean(R.styleable.MediaView_supportSmoothScroll, false);
            ta.recycle();
            setSupportSmoothScroll(supportSmoothScroll);
            setRadius(radius);
        }
    }

    public void setSupportSmoothScroll(boolean supportSmoothScroll) {
        this.supportSmoothScroll = supportSmoothScroll;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void init() {
        if (!isInitialized) {
            if (supportSmoothScroll) {
                mediaView = new HSTextureView(getContext(), radius);
                addView((HSTextureView) mediaView);
            } else {
                mediaView = new HSVideoView(getContext());
                addView((HSVideoView) mediaView);

            }
            isInitialized = true;
        }
    }

    @Override
    public void setHSBackground(Drawable drawable) {
        mediaView.setHSBackground(drawable);
    }

    @Override
    public void setHSBackground(String[] filePath) {
        mediaView.setHSBackground(filePath);
    }


    @Override
    public boolean isMedia() {
        return mediaView.isMedia();
    }

    @Override
    public void stopHSMedia() {
        if(mediaView != null) {
            mediaView.stopHSMedia();
        }
    }
}

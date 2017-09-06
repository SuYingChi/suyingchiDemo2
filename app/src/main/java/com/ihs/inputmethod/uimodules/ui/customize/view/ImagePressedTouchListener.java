package com.ihs.inputmethod.uimodules.ui.customize.view;

/**
 * Created by guonan.lv on 17/9/6.
 */

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * An {@link android.view.View.OnTouchListener} for generating pressed effect on image views.
 */
public class ImagePressedTouchListener implements View.OnTouchListener {

    /**
     * Apply a {@link android.graphics.ColorFilter} to current image set to target view. Default option.
     */
    public static final int MODE_COLOR_FILTER = 0;

    /**
     * Replace with a new drawable.
     */
    public static final int MODE_REPLACE_DRAWABLE = 1;

    private static final @ColorInt
    int TINT_COLOR = Color.argb(0x21, 0, 0, 0);

    /**
     * The {@link ImageView} that our effect applies to.
     */
    private ImageView mTargetView;
    private Drawable mReplacedDrawable;

    private int mMode;

    public ImagePressedTouchListener(ImageView targetView) {
        this(targetView, MODE_COLOR_FILTER);
    }

    public ImagePressedTouchListener(ImageView targetView, int mode) {
        mTargetView = targetView;
        mMode = mode;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mTargetView != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setPressedState();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    clearPressedState();
                    break;
            }
        }
        return false;
    }

    private void setPressedState() {
        switch (mMode) {
            case MODE_COLOR_FILTER:
                mTargetView.setColorFilter(TINT_COLOR);
                break;
            case MODE_REPLACE_DRAWABLE:
                mReplacedDrawable = mTargetView.getDrawable();
                mTargetView.setImageDrawable(new ColorDrawable(TINT_COLOR));
                break;
        }
    }

    private void clearPressedState() {
        switch (mMode) {
            case MODE_COLOR_FILTER:
                mTargetView.setColorFilter(Color.TRANSPARENT);
                break;
            case MODE_REPLACE_DRAWABLE:
                mTargetView.setImageDrawable(mReplacedDrawable);
                mReplacedDrawable = null;
                break;
        }
    }
}


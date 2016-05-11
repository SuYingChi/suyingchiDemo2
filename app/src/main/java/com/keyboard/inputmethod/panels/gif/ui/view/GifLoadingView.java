package com.keyboard.inputmethod.panels.gif.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.inputmethod.theme.HSKeyboardThemeManager;
import com.keyboard.rainbow.R;


public final class GifLoadingView extends LinearLayout {

    private View mGifLoadingCircle;
    private TextView mGifLoadingResult;

    public GifLoadingView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifLoadingView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isShowing() {
        return getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mGifLoadingCircle = findViewById(R.id.gif_loading_circle);
        mGifLoadingResult = (TextView) findViewById(R.id.gif_loading_result);
        mGifLoadingResult.setTextColor( HSKeyboardThemeManager.getTextColorFromStyleOfCurrentTheme("GifEmojiSearchView"));
    }

    public void show() {
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);

            final Animation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(1000);
            anim.setRepeatMode(Animation.RESTART);
            anim.setRepeatCount(Animation.INFINITE);
            anim.setInterpolator(new LinearInterpolator());

            mGifLoadingResult.setVisibility(View.GONE);
            mGifLoadingCircle.setVisibility(View.VISIBLE);
            mGifLoadingCircle.startAnimation(anim);

        }
    }

    public void hide() {
        mGifLoadingCircle.clearAnimation();
        mGifLoadingCircle.setVisibility(View.VISIBLE);
        mGifLoadingResult.setVisibility(View.GONE);
        setBackgroundColor(Color.TRANSPARENT);
        setVisibility(View.GONE);
    }

    public void showResult(String text) {
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }

        mGifLoadingCircle.clearAnimation();
        mGifLoadingCircle.setVisibility(View.GONE);
        mGifLoadingResult.setVisibility(View.VISIBLE);
        mGifLoadingResult.setText(text);
    }
}

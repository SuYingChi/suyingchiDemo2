package com.ihs.customtheme.app.iap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;

import com.ihs.customtheme.R;


/**
 * Created by wangzhendong on 16/4/7.
 */
public class CircleProgressView extends LinearLayout {

    private View mCircle;

    public CircleProgressView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        View content = LayoutInflater.from(context).inflate(R.layout.circle_progress_view, null);
        addView(content);
    }

    public boolean isShowing() {
        return getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mCircle = findViewById(R.id.circle);
    }

    public void start() {
        if (!isShowing()) {
            setVisibility(View.VISIBLE);

            final Animation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(1000);
            anim.setRepeatMode(Animation.RESTART);
            anim.setRepeatCount(Animation.INFINITE);
            anim.setInterpolator(new LinearInterpolator());

            mCircle.startAnimation(anim);

        }
    }

    public void stop() {
        if (isShowing()) {
            mCircle.clearAnimation();
            setVisibility(View.GONE);
        }
    }
}

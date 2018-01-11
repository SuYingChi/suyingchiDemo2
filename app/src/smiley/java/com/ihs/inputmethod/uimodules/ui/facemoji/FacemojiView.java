package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.utils.AnimationUtils;

public final class FacemojiView extends RelativeLayout {

    public FacemojiView(Context context) {
        super(context);
    }

    public FacemojiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Animation getPressAnimation() {
        return AnimationUtils.createPressAnimation();
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private void onActionDown() {
        startHostAnimation(getPressAnimation());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                onActionDown();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                startHostAnimation(AnimationUtils.createReleaseAnimation());
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    private void startHostAnimation(final Animation anim) {
        clearAnimation();
        startAnimation(anim);
    }

    public void startAnimation() {
        final FacemojiAnimationView animation = findViewById(R.id.sticker_player_view);
        animation.start();
    }

    public void stopAnimation() {
        final FacemojiAnimationView animation = findViewById(R.id.sticker_player_view);
        animation.stop();
    }
}
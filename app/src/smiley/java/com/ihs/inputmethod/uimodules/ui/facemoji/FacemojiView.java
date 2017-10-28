package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;
import com.ihs.inputmethod.uimodules.ui.facemoji.utils.AnimationUtils;

public final class FacemojiView extends RelativeLayout {

    public interface OnFacemojiEventListener {
        void onPressFacemoji();
        void onReleaseFacemoji(final FacemojiSticker facemojiSticker);

        OnFacemojiEventListener EMPTY_LISTENER = new Adapter();

        class Adapter implements OnFacemojiEventListener {
            @Override
            public void onPressFacemoji() {}
            @Override
            public void onReleaseFacemoji(final FacemojiSticker facemojiSticker) {}
        }
    }

    private OnFacemojiEventListener mListener = OnFacemojiEventListener.EMPTY_LISTENER;

    private boolean mPressed;

    private Animation.AnimationListener mReleaseAnimationListener = new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
            onReleaseAnimationEnd();
        }
        public void onAnimationRepeat(Animation animation) {}
        public void onAnimationStart(Animation animation) {}
    };

    public FacemojiView(Context context) {
        super(context);
    }

    public FacemojiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Animation getPressAnimation() {
        return AnimationUtils.createPressAnimation();
    }

    private Animation getReleaseAnimation(final Animation.AnimationListener listener) {
        final Animation releaseAnimation = AnimationUtils.createReleaseAnimation();
        releaseAnimation.setAnimationListener(listener);
        return releaseAnimation;
    }

    public boolean isFacemojiPressed() {
        return mPressed;
    }

    private void onReleaseAnimationEnd() {
        final FacemojiAnimationView facemojiView = (FacemojiAnimationView) findViewById(R.id.sticker_player_view);
        mListener.onReleaseFacemoji((FacemojiSticker) facemojiView.getTag());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private void onActionDown() {
        mPressed = true;
        startHostAnimation(getPressAnimation());
        mListener.onPressFacemoji();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                onActionDown();
                break;

            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return true;
    }

    public void release(final boolean isSelected) {
        HSLog.d("");
        if (mPressed) {
            mPressed = false;

            if (isSelected) {
                startHostAnimation(getReleaseAnimation(mReleaseAnimationListener));
            } else {
                startHostAnimation(getReleaseAnimation(null));
            }
        }
    }

    public void setOnFacemojiEventListener(final OnFacemojiEventListener listener) {
        mListener = listener;
    }

    private void startHostAnimation(final Animation anim) {
        clearAnimation();
        startAnimation(anim);
    }

    public void startAnimation() {
        final FacemojiAnimationView animation = (FacemojiAnimationView) findViewById(R.id.sticker_player_view);
        animation.start();
    }

    public void stopAnimation() {
        final FacemojiAnimationView animation = (FacemojiAnimationView) findViewById(R.id.sticker_player_view);
        animation.stop();
    }
}
package com.ihs.inputmethod.feature.lucky.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.animation.AnimatorListenerAdapter;
import com.honeycomb.launcher.util.CommonUtils;
import com.honeycomb.launcher.util.ViewUtils;

/**
 * The "GO" button on the bottom.
 */
public class GoButton extends FrameLayout {

    private enum State {
        REST,
        DOWN,
        UP,
    }
    private State mState = State.REST;

    /** Whether should go up immediately once fully down. */
    private boolean mBouncePending;

    private ImageView mBase;
    private ImageView mClickEffect;

    private boolean mHasChanceLeft;

    private final long mAnimDuration;
    private final float mPressedTranslationY;

    private ValueAnimator mAnimator;

    /** Used to clip bottom of the button with round corner to get authentic pressed-down effect. */
    private final Path mClipPath = new Path();

    private RectF mRectF = new RectF();

    public GoButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources res = context.getResources();
        mAnimDuration = res.getInteger(R.integer.config_luckyGoButtonDownUpDuration);
        mPressedTranslationY = res.getDimension(R.dimen.lucky_game_go_button_pressed_translation_y);

        initClipPath(res);
    }

    private void initClipPath(Resources res) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, R.drawable.lucky_go_button, options);
        float relativeDensity = CommonUtils.getDensityRatio() / 3f;
        int width = (int) (options.outWidth * relativeDensity);
        int height = (int) (options.outHeight * relativeDensity);

        float diameter = 2f * res.getDimension(R.dimen.lucky_game_go_button_bottom_clip_radius);

        mClipPath.lineTo(width, 0);
        mClipPath.lineTo(width, height - diameter);
        mRectF.set(width - diameter, height - diameter, width, height);
        mClipPath.arcTo(mRectF, 0f, 90f, false);
        mClipPath.lineTo(diameter, height);
        mRectF.set(0f, height - diameter, diameter, height);
        mClipPath.arcTo(mRectF, 90f, 90f, false);
        mClipPath.close();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mBase = ViewUtils.findViewById(this, R.id.lucky_game_catch_action_btn_base);
        mClickEffect = ViewUtils.findViewById(this, R.id.lucky_game_catch_action_btn_click_effect);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                setButtonPressed(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setButtonPressed(false);
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setHasChanceLeft(boolean hasChanceLeft) {
        mHasChanceLeft = hasChanceLeft;
        setClickEffect(hasChanceLeft);
        if (hasChanceLeft) {
            mClickEffect.setImageResource(R.drawable.lucky_go_button_click_effect);
        } else {
            mClickEffect.setImageResource(R.drawable.lucky_go_button_click_effect_no_chance);
        }
    }

    private void setClickEffect(boolean hasChance) {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mBase.setTranslationY(0);
        mClickEffect.setTranslationY(0);
        mClickEffect.setVisibility(hasChance ? INVISIBLE : VISIBLE);
        mState = State.REST;
        mBouncePending = false;
    }

    private void setButtonPressed(final boolean pressed) {
        if (!mHasChanceLeft) {
            return;
        }

        // Stop running animation, or schedule pop-up animation if needed
        if (mAnimator != null && mAnimator.isRunning()) {
            if (mState != State.DOWN) {
                mAnimator.cancel();
            } else {
                mBouncePending = true;
                return;
            }
        }

        float targetTranslationY = pressed ? mPressedTranslationY : 0f;
        float translationY = mBase.getTranslationY();

        // Skip if no move is needed
        if (translationY == targetTranslationY) {
            mState = State.REST;
            return;
        }

        mAnimator = ValueAnimator.ofFloat(mBase.getTranslationY(), targetTranslationY);
        mAnimator.setDuration(mAnimDuration);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationY = (float) animation.getAnimatedValue();
                mClickEffect.setVisibility(translationY > mPressedTranslationY / 2f ? VISIBLE : INVISIBLE);
                mBase.setTranslationY(translationY);
                if (pressed) {
                    mClickEffect.setTranslationY(translationY);
                }
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (pressed) {
                    mState = State.DOWN;
                } else {
                    mState = State.UP;
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator = null;
                if (mBouncePending) {
                    mBouncePending = false;
                    mState = State.UP;
                    setButtonPressed(false);
                } else {
                    mState = State.REST;
                }
            }
        });
        mAnimator.start();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.clipPath(mClipPath);
        super.dispatchDraw(canvas);
    }
}

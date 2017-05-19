package com.ihs.inputmethod.feature.boost.plus;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.honeycomb.launcher.BuildConfig;
import com.honeycomb.launcher.R;
import com.honeycomb.launcher.animation.AnimatorListenerAdapter;
import com.honeycomb.launcher.util.Thunk;
import com.honeycomb.launcher.util.ViewUtils;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Handles background color and its animation of {@link BoostPlusActivity} top banner.
 */
@SuppressWarnings("WeakerAccess")
public class BannerBackground {

    private static final String TAG = BannerBackground.class.getSimpleName();

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_VERBOSE = false && BuildConfig.DEBUG;

    private View mBgView;

    /** A chain of banner colors waiting to be animated through. */
    @Thunk Deque<Integer> mPendingColors = new ArrayDeque<>(3);
    private ValueAnimator mColorAnim;

    /** Color we're animating out of (if in animation), or current banner color (if not in animation). */
    @Thunk int mFromColor;
    /** Color we're animating towards (if in animation), or current banner color (if not in animation). */
    @Thunk int mToColor;

    public BannerBackground(Activity hostActivity, int backgroundId) {
        mBgView = ViewUtils.findViewById(hostActivity, backgroundId);

        mFromColor = mToColor = ContextCompat.getColor(getContext(), R.color.boost_plus_blue);
    }

    public void setBannerColor(@ColorRes int resId, boolean animated) {
        final @ColorInt int color = ContextCompat.getColor(getContext(), resId);
        Integer finalColor = mPendingColors.peekLast();
        if (finalColor == null) {
            finalColor = mToColor;
        }
        if (finalColor == color) {
            HSLog.d(TAG + ".Banner", "Color " + Integer.toHexString(color) + " is already the target color when all " +
                    "animation ends (if any), skip");
            return;
        }
        if (!animated) {
            HSLog.d(TAG + ".Banner", "Change banner color to " + Integer.toHexString(color) + " without animation");
            mFromColor = mToColor = color;
            mBgView.setBackgroundColor(color);
            return;
        }

        if (mColorAnim == null || !mColorAnim.isRunning()) {
            // Append the target color to end of the queue
            HSLog.d(TAG + ".Banner", "Append color " + Integer.toHexString(color) + " to end of animation queue");
            mPendingColors.offerLast(color);
            startNextColorChangeAnimation();
        }
    }

    @Thunk void startNextColorChangeAnimation() {
        Integer nextColorObj = mPendingColors.pollFirst();
        if (nextColorObj == null) {
            return;
        }
        final int nextColor = nextColorObj;
        HSLog.d(TAG + ".Banner", "Start color change anim to " + Integer.toHexString(nextColor));
        ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
        animation.setDuration(900);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private ArgbEvaluator mColorEvaluator = new ArgbEvaluator();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                int interpolatedColor = (int) mColorEvaluator.evaluate(progress, mFromColor, nextColor);
                if (DEBUG_VERBOSE) {
                    HSLog.v(TAG + ".Banner", "From " + Integer.toHexString(mFromColor) + " to "
                            + Integer.toHexString(nextColor) + ", progress " + progress);
                }
                mBgView.setBackgroundColor(interpolatedColor);
            }
        });
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mToColor = nextColor;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFromColor = nextColor;
                startNextColorChangeAnimation();
            }
        });
        mColorAnim = animation;
        animation.start();
    }

    public void stopColorChange(@ColorRes int resId) {
        if (null != mColorAnim) {
            mColorAnim.cancel();
        }
        final @ColorInt int endColor = ContextCompat.getColor(getContext(), resId);
        ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
        animation.setDuration(200);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private ArgbEvaluator mColorEvaluator = new ArgbEvaluator();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                int interpolatedColor = (int) mColorEvaluator.evaluate(progress, mFromColor, endColor);
                mBgView.setBackgroundColor(interpolatedColor);
            }
        });
        animation.start();
    }

    private Context getContext() {
        return mBgView.getContext();
    }
}

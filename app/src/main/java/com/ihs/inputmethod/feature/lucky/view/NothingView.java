package com.ihs.inputmethod.feature.lucky.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.animation.AnimatorListenerAdapter;
import com.honeycomb.launcher.animation.LauncherAnimUtils;
import com.honeycomb.launcher.lucky.LuckyActivity;
import com.honeycomb.launcher.util.ViewUtils;


public class NothingView extends FrameLayout implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private final long EMPTY_ANIMATION_DURATION_FACTOR = 1;

    private final long CIRCLE_ENLARGE_DURATION = 400 * EMPTY_ANIMATION_DURATION_FACTOR;
    private final long SMALL_CIRCLE_FADE_IN_DURATION = 100 * EMPTY_ANIMATION_DURATION_FACTOR;
    private final long CLOUD_ENLARGE_DURATION = 200 * EMPTY_ANIMATION_DURATION_FACTOR;
    private final long CLOUD_SHRINK_DURATION = 167 * EMPTY_ANIMATION_DURATION_FACTOR;
    private final long TEXT_SHRINK_DURATION = 200 * EMPTY_ANIMATION_DURATION_FACTOR;
    private final long TEXT_ENLARGE_DURATION = 167 * EMPTY_ANIMATION_DURATION_FACTOR;
    private final long CLOUD_DELAY_DURATION = 100 * EMPTY_ANIMATION_DURATION_FACTOR;
    private final long TEXT_DELAY_DURATION = 133 * EMPTY_ANIMATION_DURATION_FACTOR;
    private final long ACTION_FADE_IN_DURATION = 200 * EMPTY_ANIMATION_DURATION_FACTOR;
    private final long ACTION_FADE_IN_DELAY_DURATION = 233 * EMPTY_ANIMATION_DURATION_FACTOR;

    private View mCloud;
    private View mBigCircle;
    private View mSmallCircle;
    private View mNothingText;
    private View mAction;

    private AnimatorSet mEmptyAnimation;

    public NothingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mCloud = ViewUtils.findViewById(this, R.id.lucky_game_nothing_cloud);
        mCloud.setVisibility(INVISIBLE);
        mBigCircle = ViewUtils.findViewById(this, R.id.lucky_game_nothing_big_circle);
        mBigCircle.setVisibility(INVISIBLE);
        mSmallCircle= ViewUtils.findViewById(this, R.id.lucky_game_nothing_small_circle);
        mSmallCircle.setAlpha(0.0f);
        mNothingText = ViewUtils.findViewById(this, R.id.lucky_game_nothing_text);
        mNothingText.setVisibility(INVISIBLE);

        mAction = ViewUtils.findViewById(this, R.id.lucky_game_nothing_action);
        mAction.setAlpha(0.0f);
        mAction.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lucky_game_nothing_action:
                ((LuckyActivity) getContext()).hideAwardView("Try again");
                break;
        }
    }

    private AnimatorSet getLargeCircle() {
        mBigCircle.setScaleX(1.0f);
        mBigCircle.setScaleY(1.0f);

        ObjectAnimator circleX = ObjectAnimator.ofFloat(mBigCircle, "scaleX", 0.8f, 2.0f, 1.2f);
        circleX.setInterpolator(LauncherAnimUtils.ACCELERATE_DECELERATE);
        circleX.setDuration(CIRCLE_ENLARGE_DURATION);

        ObjectAnimator circleY = ObjectAnimator.ofFloat(mBigCircle, "scaleY", 0.8f, 2.0f, 1.2f);
        circleY.setInterpolator(LauncherAnimUtils.ACCELERATE_DECELERATE);
        circleY.setDuration(CIRCLE_ENLARGE_DURATION);

        AnimatorSet circle = new AnimatorSet();
        circle.playTogether(circleX, circleY);
        circle.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                mBigCircle.setVisibility(VISIBLE);
            }
        });
        return circle;
    }

    private AnimatorSet getSmallCircle() {
        mSmallCircle.setScaleX(1.0f);
        mSmallCircle.setScaleY(1.0f);
        mSmallCircle.setAlpha(0.0f);

        ObjectAnimator circleX = ObjectAnimator.ofFloat(mSmallCircle, "scaleX", 1.0f, 2.0f, 1.1f);
        circleX.setInterpolator(LauncherAnimUtils.ACCELERATE_DECELERATE);
        circleX.setDuration(CIRCLE_ENLARGE_DURATION);

        ObjectAnimator circleY = ObjectAnimator.ofFloat(mSmallCircle, "scaleY", 1.0f, 2.0f, 1.1f);
        circleY.setInterpolator(LauncherAnimUtils.ACCELERATE_DECELERATE);
        circleY.setDuration(CIRCLE_ENLARGE_DURATION);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mSmallCircle, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(SMALL_CIRCLE_FADE_IN_DURATION);

        AnimatorSet circle = new AnimatorSet();
        circle.playTogether(circleX, circleY, fadeIn);
        return circle;
    }

    private AnimatorSet getCloud() {
        mCloud.setScaleX(0.5f);
        mCloud.setScaleY(0.5f);

        ObjectAnimator cloudX = ObjectAnimator.ofFloat(mCloud, "scaleX", 0.5f, 1.5f);
        cloudX.setDuration(CLOUD_ENLARGE_DURATION);

        ObjectAnimator cloudY = ObjectAnimator.ofFloat(mCloud, "scaleY", 0.5f, 1.5f);
        cloudY.setDuration(CLOUD_ENLARGE_DURATION);

        ObjectAnimator cloudSX = ObjectAnimator.ofFloat(mCloud, "scaleX", 1.5f, 1.2f);
        cloudSX.setDuration(CLOUD_SHRINK_DURATION);

        ObjectAnimator cloudSY = ObjectAnimator.ofFloat(mCloud, "scaleY", 1.5f, 1.2f);
        cloudSY.setDuration(CLOUD_SHRINK_DURATION);

        AnimatorSet cloudE = new AnimatorSet();
        cloudE.playTogether(cloudX, cloudY);

        AnimatorSet cloudS = new AnimatorSet();
        cloudS.playTogether(cloudSX, cloudSY);

        AnimatorSet cloud = new AnimatorSet();
        cloud.playSequentially(cloudE, cloudS);
        cloud.setStartDelay(CLOUD_DELAY_DURATION);
        cloud.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                mCloud.setVisibility(VISIBLE);
            }
        });
        return cloud;
    }

    private AnimatorSet getText() {
        mNothingText.setScaleX(0.5f);
        mNothingText.setScaleY(0.5f);

        ObjectAnimator textX = ObjectAnimator.ofFloat(mNothingText, "scaleX", 0.5f, 1.5f);
        textX.setDuration(TEXT_ENLARGE_DURATION);

        ObjectAnimator textY = ObjectAnimator.ofFloat(mNothingText, "scaleY", 0.5f, 1.5f);
        textY.setDuration(TEXT_ENLARGE_DURATION);

        ObjectAnimator textSX = ObjectAnimator.ofFloat(mNothingText, "scaleX", 1.5f, 1.2f);
        textSX.setDuration(TEXT_SHRINK_DURATION);

        ObjectAnimator textSY = ObjectAnimator.ofFloat(mNothingText, "scaleY", 1.5f, 1.2f);
        textSY.setDuration(TEXT_SHRINK_DURATION);

        AnimatorSet textE = new AnimatorSet();
        textE.playTogether(textX, textY);

        AnimatorSet textS = new AnimatorSet();
        textS.playTogether(textSX, textSY);


        AnimatorSet text = new AnimatorSet();
        text.playSequentially(textE, textS);
        text.setStartDelay(TEXT_DELAY_DURATION);
        text.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                mNothingText.setVisibility(VISIBLE);
            }
        });
        return text;
    }

    private ObjectAnimator getAction() {
        mAction.setAlpha(0.0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mAction, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(ACTION_FADE_IN_DURATION);
        fadeIn.setStartDelay(ACTION_FADE_IN_DELAY_DURATION);
        return fadeIn;
    }

    private void reset() {
        mCloud.setVisibility(INVISIBLE);
        mBigCircle.setVisibility(INVISIBLE);
        mSmallCircle.setAlpha(0.0f);
        mNothingText.setVisibility(INVISIBLE);
        mAction.setAlpha(0.0f);
        mBigCircle.setScaleX(1.0f);
        mBigCircle.setScaleY(1.0f);
        mSmallCircle.setScaleX(1.0f);
        mSmallCircle.setScaleY(1.0f);
        mSmallCircle.setAlpha(0.0f);
        mCloud.setScaleX(0.5f);
        mCloud.setScaleY(0.5f);
        mNothingText.setScaleX(0.5f);
        mNothingText.setScaleY(0.5f);
    }

    public AnimatorSet getEmptyAnimation() {
        if (mEmptyAnimation == null) {
            mEmptyAnimation = new AnimatorSet();
            mEmptyAnimation.playTogether(getLargeCircle(), getSmallCircle(), getCloud(), getText(), getAction());
        } else {
            if (mEmptyAnimation.isRunning()) {
                mEmptyAnimation.end();
            }
            reset();
        }
        return mEmptyAnimation;
    }
}

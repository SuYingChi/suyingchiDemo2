package com.ihs.inputmethod.feature.lucky.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.feature.common.CommonUtils;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.feature.lucky.LuckyActivity;
import com.ihs.inputmethod.uimodules.R;


public class BoxView extends RelativeLayout implements View.OnClickListener {

    private static final float BOX_REDUCE_FACTOR = 0.8f;
    private static final float PEARL_REDUCE_FACTOR = 0.1f;

    private static final long BACKGROUND_FADE_IN_DURATION = 100;
    private static final long BACKGROUND_HOLD_ON_DURATION = 167;
    private static final long BOX_REDUCE_SIZE_DURATION = 200;
    private static final long BOX_ENLARGE_SIZE_DURATION = 267;
    private static final long CIRCLE_ENLARGE_SIZE_DURATION = 167;
    private static final long CIRCLE_FADE_OUT_DURATION = 100;
    private static final long RIBBON_TRANS_DELAY_DURATION = 100;
    private static final long COVER_SINGLE_TRANSLATION_UP_DURATION = 67;

    private View mContainer;

    private ImageView mBoxCover;
    private ImageView mBoxBody;
    private ImageView mBoxLight;
    private ImageView mLargeCircle;
    private ImageView mSmallCircle;
    private ImageView mRibbonLeft1;
    private ImageView mRibbonLeft2;
    private ImageView mRibbonLeft3;
    private ImageView mRibbonRight1;
    private ImageView mRibbonRight2;
    private ImageView mRibbonRight3;
    private ImageView mPearl;

    private AnimatorSet mBox;

    public BoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public static final TimeInterpolator ACCELERATE_QUAD =
            AnimationUtils.loadInterpolator(HSApplication.getContext(), R.anim.accelerate_quad);

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContainer = ViewUtils.findViewById(this, R.id.lucky_game_ad_box_container);

        mBoxCover = ViewUtils.findViewById(this, R.id.lucky_game_ad_box_cover);
        mBoxBody = ViewUtils.findViewById(this, R.id.lucky_game_ad_box);

        mLargeCircle = ViewUtils.findViewById(this, R.id.lucky_game_ad_light_circle);
        mSmallCircle = ViewUtils.findViewById(this, R.id.lucky_game_ad_light_small_circle);
        mBoxLight = ViewUtils.findViewById(this, R.id.lucky_game_ad_box_light);
        mRibbonLeft1 = ViewUtils.findViewById(this, R.id.lucky_game_ad_ribbon_left1);
        mRibbonLeft2 = ViewUtils.findViewById(this, R.id.lucky_game_ad_ribbon_left2);
        mRibbonLeft3 = ViewUtils.findViewById(this, R.id.lucky_game_ad_ribbon_left3);

        mRibbonRight1 = ViewUtils.findViewById(this, R.id.lucky_game_ad_ribbon_right1);
        mRibbonRight2 = ViewUtils.findViewById(this, R.id.lucky_game_ad_ribbon_right2);
        mRibbonRight3 = ViewUtils.findViewById(this, R.id.lucky_game_ad_ribbon_right3);

        mPearl = ViewUtils.findViewById(this, R.id.lucky_game_ad_box_pearl);

        final View cancel = ViewUtils.findViewById(this, R.id.lucky_game_ad_action_cancel);
        cancel.setOnClickListener(this);
        post(new Runnable() {
            @Override
            public void run() {
                Rect rect = new Rect();
                cancel.getHitRect(rect);
                final  int padding = CommonUtils.pxFromDp(10);
                rect.left -= padding;
                rect.top -= padding;
                rect.bottom += padding;
                rect.right += padding;
                BoxView.this.setTouchDelegate(new TouchDelegate(rect, cancel));
            }
        });
    }

    private AnimatorSet getFadeInAnimation() {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(this, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(BACKGROUND_FADE_IN_DURATION);
        fadeIn.addListener(new com.ihs.inputmethod.feature.common.AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setAlpha(0.0f);
                setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mContainer.setAlpha(1.0f);
                mContainer.setVisibility(VISIBLE);
            }
        });

        ObjectAnimator holdOn = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 1.0f);
        holdOn.setDuration(BACKGROUND_HOLD_ON_DURATION);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(fadeIn, holdOn);
        return set;
    }

    private AnimatorSet getBoxDownAnimation() {
        ObjectAnimator boxTransDown = ObjectAnimator.ofFloat(mBoxBody, "translationY", -100, 0);
        boxTransDown.setDuration(BOX_ENLARGE_SIZE_DURATION - COVER_SINGLE_TRANSLATION_UP_DURATION);

        ObjectAnimator boxLightTransDown = ObjectAnimator.ofFloat(mBoxLight, "translationY", -90, 0);
        boxLightTransDown.setDuration(BOX_ENLARGE_SIZE_DURATION - COVER_SINGLE_TRANSLATION_UP_DURATION);

        ObjectAnimator coverTransDown2 = ObjectAnimator.ofFloat(mBoxCover, "translationY", -100, 0);
        coverTransDown2.setDuration(BOX_ENLARGE_SIZE_DURATION - COVER_SINGLE_TRANSLATION_UP_DURATION);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(boxTransDown, coverTransDown2, boxLightTransDown);
        return set;
    }

    private AnimatorSet getCircleAnimation() {
        mLargeCircle.setScaleX(0.0f);
        mLargeCircle.setScaleY(0.0f);
        mLargeCircle.setAlpha(1.0f);

        mSmallCircle.setScaleX(0.0f);
        mSmallCircle.setScaleY(0.0f);
        mSmallCircle.setAlpha(1.0f);

        ObjectAnimator enlargeCircleWidth = ObjectAnimator.ofFloat(mLargeCircle, "scaleX", 1, 3);
        enlargeCircleWidth.setDuration(CIRCLE_ENLARGE_SIZE_DURATION);
        ObjectAnimator enlargeCircleHeight = ObjectAnimator.ofFloat(mLargeCircle, "scaleY", 1, 3);
        enlargeCircleHeight.setDuration(CIRCLE_ENLARGE_SIZE_DURATION);

        ObjectAnimator circleFadeOut = ObjectAnimator.ofFloat(mLargeCircle, "alpha", 1.0f, 0.0f);
        circleFadeOut.setDuration(CIRCLE_FADE_OUT_DURATION);

        ObjectAnimator enlargeSmallCircleWidth = ObjectAnimator.ofFloat(mSmallCircle, "scaleX", 1, 2.3f);
        enlargeSmallCircleWidth.setDuration((long) (CIRCLE_ENLARGE_SIZE_DURATION * 2.3f / 3));
        ObjectAnimator enlargeSmallCircleHeight = ObjectAnimator.ofFloat(mSmallCircle, "scaleY", 1, 2.3f);
        enlargeSmallCircleHeight.setDuration((long) (CIRCLE_ENLARGE_SIZE_DURATION * 2.3f / 3));

        ObjectAnimator smallCircleFadeOut = ObjectAnimator.ofFloat(mSmallCircle, "alpha", 1.0f, 0.0f);
        smallCircleFadeOut.setDuration(CIRCLE_FADE_OUT_DURATION);

        AnimatorSet circle = new AnimatorSet();
        circle.playTogether(enlargeCircleWidth, enlargeCircleHeight, enlargeSmallCircleWidth, enlargeSmallCircleHeight);

        AnimatorSet circleOut = new AnimatorSet();
        circleOut.playTogether(circleFadeOut, smallCircleFadeOut);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(circle, circleOut);
        return set;
    }

    private AnimatorSet getEnlargeBoxAnimation() {
        ObjectAnimator enlargeBoxWidth = ObjectAnimator.ofFloat(mBoxBody, "scaleX", BOX_REDUCE_FACTOR, 1.0f);
        enlargeBoxWidth.setDuration(BOX_ENLARGE_SIZE_DURATION);
        ObjectAnimator enlargeBoxHeight = ObjectAnimator.ofFloat(mBoxBody, "scaleY", BOX_REDUCE_FACTOR, 1.0f);
        enlargeBoxHeight.setDuration(BOX_ENLARGE_SIZE_DURATION);

        ObjectAnimator enlargeBoxLightX = ObjectAnimator.ofFloat(mBoxLight, "scaleX", BOX_REDUCE_FACTOR * 0.8f, 1.0f);
        enlargeBoxLightX.setDuration(BOX_ENLARGE_SIZE_DURATION);
        ObjectAnimator enlargeBoxLightY = ObjectAnimator.ofFloat(mBoxLight, "scaleY", BOX_REDUCE_FACTOR, 1.0f);
        enlargeBoxLightY.setDuration(BOX_ENLARGE_SIZE_DURATION);

        ObjectAnimator enlargeCoverWidth = ObjectAnimator.ofFloat(mBoxCover, "scaleX", BOX_REDUCE_FACTOR, 1.0f);
        enlargeCoverWidth.setDuration(BOX_ENLARGE_SIZE_DURATION);
        ObjectAnimator enlargeCoverHeight = ObjectAnimator.ofFloat(mBoxCover, "scaleY", BOX_REDUCE_FACTOR, 1.0f);
        enlargeCoverHeight.setDuration(BOX_ENLARGE_SIZE_DURATION);

        AnimatorSet enlarge = new AnimatorSet();
        enlarge.playTogether(enlargeBoxWidth, enlargeBoxHeight, enlargeCoverWidth, enlargeCoverHeight, enlargeBoxLightX, enlargeBoxLightY);
        return enlarge;
    }

    private AnimatorSet getRibbonAnimation() {
        mRibbonLeft1.setTranslationX(0.0f);
        mRibbonLeft1.setTranslationY(0.0f);
        mRibbonLeft1.setAlpha(0.0f);

        mRibbonLeft2.setTranslationX(0.0f);
        mRibbonLeft2.setTranslationY(0.0f);
        mRibbonLeft2.setAlpha(0.0f);

        mRibbonLeft3.setTranslationX(0.0f);
        mRibbonLeft3.setTranslationY(0.0f);
        mRibbonLeft3.setAlpha(0.0f);

        ObjectAnimator transXRibbonL1 = ObjectAnimator.ofFloat(mRibbonLeft1, "translationX", 0.0f, -300.0f);
        transXRibbonL1.setDuration(BOX_ENLARGE_SIZE_DURATION);
        transXRibbonL1.setInterpolator(ACCELERATE_QUAD);
        ObjectAnimator transYRibbonL1 = ObjectAnimator.ofFloat(mRibbonLeft1, "translationY", 0.0f, -200.0f);
        transYRibbonL1.setDuration(BOX_ENLARGE_SIZE_DURATION);
        transYRibbonL1.setInterpolator(ACCELERATE_QUAD);

        ObjectAnimator ribbonFadeOutL1 = ObjectAnimator.ofFloat(mRibbonLeft1, "alpha", 0, 1, 0);
        ribbonFadeOutL1.setDuration(BOX_ENLARGE_SIZE_DURATION);

        ObjectAnimator transXRibbonL2 = ObjectAnimator.ofFloat(mRibbonLeft2, "translationX", 0.0f, -250.0f);
        transXRibbonL2.setDuration(BOX_ENLARGE_SIZE_DURATION);
        transXRibbonL2.setInterpolator(ACCELERATE_QUAD);
        ObjectAnimator transYRibbonL2 = ObjectAnimator.ofFloat(mRibbonLeft2, "translationY", 0.0f, -120.0f);
        transYRibbonL2.setDuration(BOX_ENLARGE_SIZE_DURATION);
        transYRibbonL2.setInterpolator(ACCELERATE_QUAD);

        ObjectAnimator ribbonFadeOutL2 = ObjectAnimator.ofFloat(mRibbonLeft2, "alpha", 0, 1, 0);
        ribbonFadeOutL2.setDuration(BOX_ENLARGE_SIZE_DURATION);

        ObjectAnimator transXRibbonL3 = ObjectAnimator.ofFloat(mRibbonLeft3, "translationX", 0.0f, -200.0f);
        transXRibbonL3.setDuration(BOX_ENLARGE_SIZE_DURATION - RIBBON_TRANS_DELAY_DURATION);
        transXRibbonL3.setStartDelay(RIBBON_TRANS_DELAY_DURATION);
        transXRibbonL3.setInterpolator(ACCELERATE_QUAD);
        ObjectAnimator transYRibbonL3 = ObjectAnimator.ofFloat(mRibbonLeft3, "translationY", 0.0f, -150.0f);
        transYRibbonL3.setDuration(BOX_ENLARGE_SIZE_DURATION - RIBBON_TRANS_DELAY_DURATION);
        transYRibbonL3.setStartDelay(RIBBON_TRANS_DELAY_DURATION);
        transYRibbonL3.setInterpolator(ACCELERATE_QUAD);

        ObjectAnimator ribbonFadeOutL3 = ObjectAnimator.ofFloat(mRibbonLeft3, "alpha", 0, 1, 0);
        ribbonFadeOutL3.setDuration(BOX_ENLARGE_SIZE_DURATION - RIBBON_TRANS_DELAY_DURATION);
        ribbonFadeOutL3.setStartDelay(RIBBON_TRANS_DELAY_DURATION);

        mRibbonRight1.setTranslationX(0.0f);
        mRibbonRight1.setTranslationY(0.0f);
        mRibbonRight1.setAlpha(0.0f);

        mRibbonRight2.setTranslationX(0.0f);
        mRibbonRight2.setTranslationY(0.0f);
        mRibbonRight2.setAlpha(0.0f);

        mRibbonRight3.setTranslationX(0.0f);
        mRibbonRight3.setTranslationY(0.0f);
        mRibbonRight3.setAlpha(0.0f);

        ObjectAnimator transXRibbonR1 = ObjectAnimator.ofFloat(mRibbonRight1, "translationX", 0.0f, 200.0f);
        transXRibbonR1.setDuration(BOX_ENLARGE_SIZE_DURATION);
        transXRibbonR1.setInterpolator(ACCELERATE_QUAD);
        ObjectAnimator transYRibbonR1 = ObjectAnimator.ofFloat(mRibbonRight1, "translationY", 0.0f, -150.0f);
        transYRibbonR1.setDuration(BOX_ENLARGE_SIZE_DURATION);
        transYRibbonR1.setInterpolator(ACCELERATE_QUAD);

        ObjectAnimator ribbonFadeOutR1 = ObjectAnimator.ofFloat(mRibbonRight1, "alpha", 0, 1, 0);
        ribbonFadeOutR1.setDuration(BOX_ENLARGE_SIZE_DURATION);

        ObjectAnimator transXRibbonR2 = ObjectAnimator.ofFloat(mRibbonRight2, "translationX", 0.0f, 200.0f);
        transXRibbonR2.setDuration(BOX_ENLARGE_SIZE_DURATION);
        transXRibbonR2.setInterpolator(ACCELERATE_QUAD);
        ObjectAnimator transYRibbonR2 = ObjectAnimator.ofFloat(mRibbonRight2, "translationY", 0.0f, -150.0f);
        transYRibbonR2.setDuration(BOX_ENLARGE_SIZE_DURATION);
        transYRibbonR2.setInterpolator(ACCELERATE_QUAD);

        ObjectAnimator ribbonFadeOutR2 = ObjectAnimator.ofFloat(mRibbonRight2, "alpha", 0, 1, 0);
        ribbonFadeOutR2.setDuration(BOX_ENLARGE_SIZE_DURATION);

        ObjectAnimator transXRibbonR3 = ObjectAnimator.ofFloat(mRibbonRight3, "translationX", 0.0f, 200.0f);
        transXRibbonR3.setDuration(BOX_ENLARGE_SIZE_DURATION - RIBBON_TRANS_DELAY_DURATION);
        transXRibbonR3.setStartDelay(RIBBON_TRANS_DELAY_DURATION);
        transXRibbonR3.setInterpolator(ACCELERATE_QUAD);
        ObjectAnimator transYRibbonR3 = ObjectAnimator.ofFloat(mRibbonRight3, "translationY", 0.0f, -150.0f);
        transYRibbonR3.setDuration(BOX_ENLARGE_SIZE_DURATION - RIBBON_TRANS_DELAY_DURATION);
        transYRibbonR3.setStartDelay(RIBBON_TRANS_DELAY_DURATION);
        transYRibbonR3.setInterpolator(ACCELERATE_QUAD);

        ObjectAnimator ribbonFadeOutR3 = ObjectAnimator.ofFloat(mRibbonRight3, "alpha", 0, 1, 0);
        ribbonFadeOutR3.setDuration(BOX_ENLARGE_SIZE_DURATION - RIBBON_TRANS_DELAY_DURATION);
        ribbonFadeOutR3.setStartDelay(RIBBON_TRANS_DELAY_DURATION);

        AnimatorSet ribbon = new AnimatorSet();
        ribbon.playTogether(transXRibbonL1, transYRibbonL1, ribbonFadeOutL1, transXRibbonL2, transYRibbonL2, ribbonFadeOutL2,
                transXRibbonL3, transYRibbonL3, ribbonFadeOutL3, transXRibbonR1, transYRibbonR1, ribbonFadeOutR1,
                transXRibbonR2, transYRibbonR2, ribbonFadeOutR2, transXRibbonR3, transYRibbonR3, ribbonFadeOutR3);
        return ribbon;
    }

    private AnimatorSet getShrinkBoxAnimation() {
        ObjectAnimator reduceBoxWidth = ObjectAnimator.ofFloat(mBoxBody, "scaleX", 1.0f, BOX_REDUCE_FACTOR);
        reduceBoxWidth.setDuration(BOX_REDUCE_SIZE_DURATION);
        ObjectAnimator reduceBoxHeight = ObjectAnimator.ofFloat(mBoxBody, "scaleY", 1.0f, BOX_REDUCE_FACTOR);
        reduceBoxHeight.setDuration(BOX_REDUCE_SIZE_DURATION);

        ObjectAnimator reduceBoxLightX = ObjectAnimator.ofFloat(mBoxLight, "scaleX", 1.0f, BOX_REDUCE_FACTOR * 0.8f);
        reduceBoxLightX.setDuration(BOX_REDUCE_SIZE_DURATION);
        ObjectAnimator reduceBoxLightY = ObjectAnimator.ofFloat(mBoxLight, "scaleY", 1.0f, BOX_REDUCE_FACTOR);
        reduceBoxLightY.setDuration(BOX_REDUCE_SIZE_DURATION);

        ObjectAnimator reduceCoverWidth = ObjectAnimator.ofFloat(mBoxCover, "scaleX", 1.0f, BOX_REDUCE_FACTOR);
        reduceCoverWidth.setDuration(BOX_REDUCE_SIZE_DURATION);
        ObjectAnimator reduceCoverHeight = ObjectAnimator.ofFloat(mBoxCover, "scaleY", 1.0f, BOX_REDUCE_FACTOR);
        reduceCoverHeight.setDuration(BOX_REDUCE_SIZE_DURATION);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(reduceBoxWidth, reduceBoxHeight, reduceBoxLightX, reduceBoxLightY, reduceCoverWidth, reduceCoverHeight);
        return set;
    }

    private AnimatorSet getBoxUpAnimation() {
        //translation box and cover
        ObjectAnimator boxTransUp = ObjectAnimator.ofFloat(mBoxBody, "translationY", 0, -100);
        boxTransUp.setDuration(BOX_REDUCE_SIZE_DURATION - COVER_SINGLE_TRANSLATION_UP_DURATION);

        ObjectAnimator boxLightTransUp = ObjectAnimator.ofFloat(mBoxLight, "translationY", 0, -90);
        boxLightTransUp.setDuration(BOX_REDUCE_SIZE_DURATION - COVER_SINGLE_TRANSLATION_UP_DURATION);

        ObjectAnimator coverTransUp = ObjectAnimator.ofFloat(mBoxCover, "translationY", 0, -100);
        coverTransUp.setDuration(BOX_REDUCE_SIZE_DURATION - COVER_SINGLE_TRANSLATION_UP_DURATION);

        AnimatorSet transTogether = new AnimatorSet();
        transTogether.playTogether(boxTransUp, coverTransUp, boxLightTransUp);
        return transTogether;
    }

    private ObjectAnimator getCoverDownAnimation() {
        ObjectAnimator coverTransDown = ObjectAnimator.ofFloat(mBoxCover, "translationY", -130, -100);
        coverTransDown.setDuration(COVER_SINGLE_TRANSLATION_UP_DURATION);
        return coverTransDown;
    }

    private ObjectAnimator getCoverUpAnimation() {
        ObjectAnimator coverTransUp2 = ObjectAnimator.ofFloat(mBoxCover, "translationY", -100, -130);
        coverTransUp2.setDuration(COVER_SINGLE_TRANSLATION_UP_DURATION);
        return coverTransUp2;
    }

    private AnimatorSet getPearlDownAnimation() {
        mPearl.setScaleX(0.0f);
        mPearl.setScaleY(0.0f);
        mPearl.setAlpha(1.0f);
        ObjectAnimator pearlTransDown = ObjectAnimator.ofFloat(mPearl, "translationY", -80, 0);
        pearlTransDown.setInterpolator( AnimationUtils.loadInterpolator(HSApplication.getContext(), R.anim.decelerate_quad));
        pearlTransDown.setDuration(BOX_REDUCE_SIZE_DURATION - COVER_SINGLE_TRANSLATION_UP_DURATION);
        ObjectAnimator enlargePearlX = ObjectAnimator.ofFloat(mPearl, "scaleX", PEARL_REDUCE_FACTOR, BOX_REDUCE_FACTOR);
        enlargePearlX.setDuration(BOX_REDUCE_SIZE_DURATION);
        ObjectAnimator enlargePearlY = ObjectAnimator.ofFloat(mPearl, "scaleY", PEARL_REDUCE_FACTOR, BOX_REDUCE_FACTOR);
        enlargePearlY.setDuration(BOX_REDUCE_SIZE_DURATION);
        ObjectAnimator pearlFadeOut = ObjectAnimator.ofFloat(mPearl, "alpha", 0.0f, 1.0f, 0.0f, 1.0f);
        pearlFadeOut.setInterpolator(ACCELERATE_QUAD);
        pearlFadeOut.setDuration(BOX_REDUCE_SIZE_DURATION);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(pearlTransDown, enlargePearlX, enlargePearlY, pearlFadeOut);
        return set;
    }

    private AnimatorSet getPearlUpAnimation() {
        mPearl.setScaleX(0.0f);
        mPearl.setScaleY(0.0f);
        mPearl.setTranslationY(0.0f);
        mPearl.setAlpha(1.0f);

        ObjectAnimator pearlTransUp = ObjectAnimator.ofFloat(mPearl, "translationY", 0, -80);
        pearlTransUp.setDuration(BOX_REDUCE_SIZE_DURATION - COVER_SINGLE_TRANSLATION_UP_DURATION);
        ObjectAnimator enlargePearlX = ObjectAnimator.ofFloat(mPearl, "scaleX", PEARL_REDUCE_FACTOR, BOX_REDUCE_FACTOR);
        enlargePearlX.setDuration(BOX_REDUCE_SIZE_DURATION);
        ObjectAnimator enlargePearlY = ObjectAnimator.ofFloat(mPearl, "scaleY", PEARL_REDUCE_FACTOR, BOX_REDUCE_FACTOR);
        enlargePearlY.setDuration(BOX_REDUCE_SIZE_DURATION);
        ObjectAnimator pearlFadeIn = ObjectAnimator.ofFloat(mPearl, "alpha", 0.0f, 1.0f);
        pearlFadeIn.setDuration(BOX_REDUCE_SIZE_DURATION);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(pearlTransUp, enlargePearlX, enlargePearlY, pearlFadeIn);
        return set;
    }

    private void reset() {

    }

    public AnimatorSet getBoxAnimation() {
        if (mBox == null) {
            mBox = new AnimatorSet();
            AnimatorSet boxUp = new AnimatorSet();
            boxUp.playSequentially(getBoxUpAnimation(), getCoverUpAnimation());

            AnimatorSet transUp = new AnimatorSet();
            transUp.playTogether(getShrinkBoxAnimation(), boxUp, getPearlUpAnimation());

            AnimatorSet transDownTogether = new AnimatorSet();
            transDownTogether.playTogether(getBoxDownAnimation(), getPearlDownAnimation());

            AnimatorSet transDown = new AnimatorSet();
            transDown.playSequentially(getCoverDownAnimation(), transDownTogether);

            AnimatorSet enlarge = new AnimatorSet();
            enlarge.playTogether(getEnlargeBoxAnimation(), transDown, getRibbonAnimation());

            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mContainer, "alpha", 1.0f, 0.5f);
            fadeOut.setDuration(300);
            mBox.playSequentially(getFadeInAnimation(), transUp, enlarge, getCircleAnimation(), fadeOut);
        } else {
            if (mBox.isRunning()) {
                mBox.end();
            }
        }

        return mBox;
    }

    public void setBoxBodyBitmap(Bitmap body) {
        mBoxBody.setImageBitmap(body);
    }

    public void setBoxCoverBitmap(Bitmap cover) {
        mBoxCover.setImageBitmap(cover);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lucky_game_ad_action_cancel:
                ((LuckyActivity) getContext()).hideAwardView("Close");
                break;
        }
    }
}

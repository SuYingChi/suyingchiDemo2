package com.ihs.inputmethod.feature.lucky.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ihs.feature.common.Utils;
import com.ihs.inputmethod.uimodules.R;

import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;


public abstract class FlyAwardBaseView extends RelativeLayout {

    protected static final long QUESTION_MARK_UP_DURATION = 333;
    protected static final long QUESTION_MARK_HOLD_ON_DURATION = 133;
    protected static final long ICON_FLIP_DURATION = 267;

    protected long xCoordsTrans;
    protected long yCoordsTrans;
    protected long yCoordsTransFlip;

    protected AcbNativeAdIconView mIcon;
    protected ImageView mDragIcon;

    public FlyAwardBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void setTranslationYProgress(float progress) {
        float translationY = progress * yCoordsTrans;
        mDragIcon.setTranslationY(translationY);
    }

    protected void setFlipTranslationYProgress(float progress) {
        float translationY = progress * yCoordsTransFlip + yCoordsTrans;
        mDragIcon.setTranslationY(translationY);
    }

    protected void setFlipTranslationXProgress(float progress) {
        float translationX = progress * xCoordsTrans;
        mDragIcon.setTranslationX(translationX);
    }

    protected void calculateAnimationDistance(final View root) {

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int[] coord1 = new int[2];
                Utils.getDescendantCoordRelativeToParent(mIcon, root, coord1, false);

                int[] coord2 = new int[2];
                Utils.getDescendantCoordRelativeToParent(mDragIcon, root, coord2, false);

                yCoordsTrans = (coord1[1] - coord2[1]) * 9 / 10;
                yCoordsTransFlip = (coord1[1] - coord2[1]) / 10;
                xCoordsTrans = coord1[0] - coord2[0];
                if (xCoordsTrans != 0) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    protected AnimatorSet dragUp() {
        mDragIcon.setAlpha(0.0f);
        mDragIcon.setScaleX(1.0f);
        mDragIcon.setScaleY(1.0f);
        mDragIcon.setTranslationX(0.0f);
        mDragIcon.setTranslationY(0.0f);
        mDragIcon.setRotationY(0);
        mDragIcon.setVisibility(VISIBLE);
        mDragIcon.setImageResource(R.drawable.lucky_award_ad_question_mark);

        ObjectAnimator initRotationY = ObjectAnimator.ofFloat(mDragIcon, "rotationY", 0, 180);
        initRotationY.setDuration(0);

        ObjectAnimator dragAlpha = ObjectAnimator.ofFloat(mDragIcon, "alpha", 0.0f, 1.0f);
        dragAlpha.setDuration(QUESTION_MARK_UP_DURATION);


        ObjectAnimator dragTrans = ObjectAnimator.ofFloat(this, "translationYProgress", 0, 1f);
        dragTrans.setDuration(QUESTION_MARK_UP_DURATION);

        ObjectAnimator dragScaleWidth = ObjectAnimator.ofFloat(mDragIcon, "scaleX", 1.0f, 3.5f);
        dragScaleWidth.setDuration(QUESTION_MARK_UP_DURATION);
        ObjectAnimator dragScaleHeight = ObjectAnimator.ofFloat(mDragIcon, "scaleY", 1.0f, 3.5f);
        dragScaleHeight.setDuration(QUESTION_MARK_UP_DURATION);

        AnimatorSet dragUp = new AnimatorSet();
        dragUp.playTogether(initRotationY, dragAlpha, dragTrans, dragScaleWidth, dragScaleHeight);

        return dragUp;
    }

    protected AnimatorSet flipToIcon() {
        ObjectAnimator flip = ObjectAnimator.ofFloat(mDragIcon, "rotationY", 180, 360);
        flip.setDuration(ICON_FLIP_DURATION);
        ObjectAnimator dragWidth = ObjectAnimator.ofFloat(mDragIcon, "scaleX", 3.5f, 1.0f);
        dragWidth.setDuration(ICON_FLIP_DURATION);

        ObjectAnimator dragHeight = ObjectAnimator.ofFloat(mDragIcon, "scaleY", 3.5f, 1.0f);
        dragHeight.setDuration(ICON_FLIP_DURATION);

        ObjectAnimator dragToIconX = ObjectAnimator.ofFloat(this, "flipTranslationXProgress", 0, 1f);
        dragToIconX.setDuration(ICON_FLIP_DURATION);

        ObjectAnimator dragToIconY = ObjectAnimator.ofFloat(this, "flipTranslationYProgress", 0, 1f);
        dragToIconY.setDuration(ICON_FLIP_DURATION);

        AnimatorSet flipToIcon = new AnimatorSet();
        flipToIcon.playTogether(flip, dragWidth, dragHeight, dragToIconX, dragToIconY);
        return flipToIcon;
    }

    protected ObjectAnimator fadeIn(final View view) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0, 1.0f);
        fadeIn.setDuration(QUESTION_MARK_UP_DURATION);
        return fadeIn;
    }

}

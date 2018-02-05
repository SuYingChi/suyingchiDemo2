package com.ihs.inputmethod.feature.lucky.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.view.View;
import android.view.ViewTreeObserver;

import com.ihs.feature.common.Utils;
import com.ihs.inputmethod.uimodules.R;

@SuppressWarnings("unused")
class ChancesAnimationAdapter extends AnimatorListenerAdapter {

    private static final String TAG = ChancesAnimationAdapter.class.getSimpleName();

    private @IdRes
    int[] FRAGMENT_RES_IDS = {
            R.id.lucky_game_award_chances_fragment_1,
            R.id.lucky_game_award_chances_fragment_2,
            R.id.lucky_game_award_chances_fragment_3,
            R.id.lucky_game_award_chances_fragment_4,
            R.id.lucky_game_award_chances_fragment_5,
    };

    // Animated components
    private View mTitle;
    private View mArm;
    private View mBubble;
    private View mStars;
    private View mHalo;
    private View mAboveLight;
    private View mCentralLight;
    private View mDescription;
    private View mReceiveBtn;

    private View[] mFragments = new View[FRAGMENT_RES_IDS.length];

    private int mInitialArmTranslationY;
    private final float mFragmentTranslationY;

    ChancesAnimationAdapter(final View chancesView) {
        mTitle = chancesView.findViewById(R.id.lucky_game_award_chances_title);
        mArm = chancesView.findViewById(R.id.lucky_game_award_chances_arm);
        mBubble = chancesView.findViewById(R.id.lucky_game_award_chances_bubble);
        mStars = chancesView.findViewById(R.id.lucky_game_award_chances_stars);
        mHalo = chancesView.findViewById(R.id.lucky_game_award_chances_halo);
        mAboveLight = chancesView.findViewById(R.id.lucky_game_award_chances_above_light);
        mCentralLight = chancesView.findViewById(R.id.lucky_game_award_chances_centralized_light);
        mDescription = chancesView.findViewById(R.id.lucky_game_award_chances_description);
        mReceiveBtn = chancesView.findViewById(R.id.lucky_game_award_chances_receive_btn);
        View fragmentsContainer = chancesView.findViewById(R.id.lucky_game_award_chances_fragments);
        for (int i = 0; i < FRAGMENT_RES_IDS.length; i++) {
            mFragments[i] = fragmentsContainer.findViewById(FRAGMENT_RES_IDS[i]);
        }

        final Resources res = chancesView.getContext().getResources();
        chancesView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int[] coord = new int[2];
                Utils.getDescendantCoordRelativeToParent(mReceiveBtn, chancesView, coord, false);
                int boxY = coord[1] + mReceiveBtn.getHeight() + res.getDimensionPixelOffset(
                        R.dimen.lucky_game_award_chances_initial_arm_offset_below_receive_btn);
                coord[0] = coord[1] = 0;
                Utils.getDescendantCoordRelativeToParent(mArm, chancesView, coord, false);
                int armY = coord[1];
                mInitialArmTranslationY = boxY - armY;

                if (armY != 0) {
                    chancesView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        mFragmentTranslationY = res.getDimension(
                R.dimen.lucky_game_award_chances_fragment_translation_y);

        reset();
    }

    @Override
    public void onAnimationStart(Animator animation) {
        reset();
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        reset();
    }

    private void reset() {
        mTitle.setVisibility(View.INVISIBLE);
        mArm.setVisibility(View.INVISIBLE);
        mBubble.setVisibility(View.INVISIBLE);
        mStars.setVisibility(View.INVISIBLE);
        mHalo.setVisibility(View.INVISIBLE);
        mAboveLight.setVisibility(View.INVISIBLE);
        mCentralLight.setVisibility(View.INVISIBLE);
        mDescription.setVisibility(View.INVISIBLE);
        mReceiveBtn.setVisibility(View.INVISIBLE);
        for (View fragment : mFragments) {
            fragment.setVisibility(View.INVISIBLE);
        }
    }

    public void setArmAlphaScaleTranslationYProgress(float progress) {
        mArm.setVisibility(View.VISIBLE);

        mArm.setAlpha(progress);

        float scaleXY = 1.5f * progress;
        mArm.setScaleX(scaleXY);
        mArm.setScaleY(scaleXY);

        float translationY = (1f - progress) * mInitialArmTranslationY;
        mArm.setTranslationY(translationY);
    }

    public void setArmScale(float scaleXY) {
        mArm.setVisibility(View.VISIBLE);

        mArm.setScaleX(scaleXY);
        mArm.setScaleY(scaleXY);
    }

    public void setBubbleAlphaScale(float alphaScale) {
        mBubble.setVisibility(View.VISIBLE);

        mBubble.setAlpha(alphaScale);

        mBubble.setScaleX(alphaScale);
        mBubble.setScaleY(alphaScale);
    }

    public void setHaloAlphaScale(float alphaScale) {
        mHalo.setVisibility(View.VISIBLE);

        mHalo.setAlpha(alphaScale);

        mHalo.setScaleX(alphaScale);
        mHalo.setScaleY(alphaScale);
    }

    public void setTitleAndButtonsAlpha(float alpha) {
        mTitle.setVisibility(View.VISIBLE);
        mReceiveBtn.setVisibility(View.VISIBLE);

        mTitle.setAlpha(alpha);
        mReceiveBtn.setAlpha(alpha);
    }

    public void setDescriptionAlphaScale(float alphaScale) {
        mDescription.setVisibility(View.VISIBLE);

        mDescription.setAlpha(alphaScale);

        mDescription.setScaleX(alphaScale);
        mDescription.setScaleY(alphaScale);
    }

    public void setStarsAndLightsAlpha(float alpha) {
        mStars.setVisibility(View.VISIBLE);
        mAboveLight.setVisibility(View.VISIBLE);
        mCentralLight.setVisibility(View.VISIBLE);

        mStars.setAlpha(alpha);
        mAboveLight.setAlpha(alpha);
        mCentralLight.setAlpha(alpha);
    }

    public void setStarsAlpha(float alpha) {
        mStars.setVisibility(View.VISIBLE);

        mStars.setAlpha(alpha);
    }

    public void setFragmentAlphaTranslationYProgress1(float progress) {
        setFragmentAlphaTranslationYProgress(0, progress);
    }

    public void setFragmentAlphaTranslationYProgress2(float progress) {
        setFragmentAlphaTranslationYProgress(1, progress);
    }

    public void setFragmentAlphaTranslationYProgress3(float progress) {
        setFragmentAlphaTranslationYProgress(2, progress);
    }

    public void setFragmentAlphaTranslationYProgress4(float progress) {
        setFragmentAlphaTranslationYProgress(3, progress);
    }

    public void setFragmentAlphaTranslationYProgress5(float progress) {
        setFragmentAlphaTranslationYProgress(4, progress);
    }

    private void setFragmentAlphaTranslationYProgress(int index, float progress) {
        View fragment = mFragments[index];
        fragment.setVisibility(View.VISIBLE);

        fragment.setTranslationY((progress - 0.5f) * mFragmentTranslationY);
        float alpha;
        if (progress < 0.2f) {
            alpha = progress / 0.2f;
        } else if (progress < 0.5f) {
            alpha = 1f;
        } else {
            alpha = 1f - (progress - 0.5f) / 0.5f;
        }
        fragment.setAlpha(alpha);
    }

    public void setLightRotateDegrees(float degrees) {
        mCentralLight.setVisibility(View.VISIBLE);

        mCentralLight.setRotation(degrees);
    }
}

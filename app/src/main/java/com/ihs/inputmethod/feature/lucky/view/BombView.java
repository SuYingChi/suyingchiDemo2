package com.ihs.inputmethod.feature.lucky.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ihs.inputmethod.feature.common.CommonUtils;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.feature.lucky.LuckyActivity;
import com.ihs.inputmethod.uimodules.R;

import java.util.Locale;


public class BombView extends FrameLayout implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private static final long ICON_TRANSLATION_DURATION = 33;
    private static final long ICON_DISAPPEAR_DURATION = 100;
    private static final long EFFECT_FADE_IN_DURATION = 133;
    private static final long EFFECT_ENLARGE_DURATION = 100;
    private static final long LIGHT_ENLARGE_DURATION = 67;
    private static final long TEXT_FADE_IN_DURATION = 67;
    private static final long TEXT_SHRINK_DURATION = 200;
    private static final long FRAGMENT_ENLARGE_DURATION = 200;
    private static final long DESCRIPTION_FADE_IN_DURATION = 100;
    private static final long FRAGMENT_FADE_OUT_DURATION = 300;
    private static final long FIRE_FADE_OUT_DURATION = 367;

    private View mLight;
    private View mFragment;
    private View mEffect;
    private TextView mDescription;
    private View mBomb;
    private View mFire;
    private View mText;
    private View mClose;

    public BombView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mFire = ViewUtils.findViewById(this, R.id.lucky_game_bomb_fire);
        mLight =ViewUtils.findViewById(this, R.id.lucky_game_bomb_light);
        mFragment = ViewUtils.findViewById(this, R.id.lucky_game_bomb_frame);
        mEffect = ViewUtils.findViewById(this, R.id.lucky_game_bomb_effect);
        mDescription = ViewUtils.findViewById(this, R.id.lucky_game_bomb_description);
        mBomb = ViewUtils.findViewById(this, R.id.lucky_game_boom_icon);
        mText = ViewUtils.findViewById(this, R.id.lucky_game_bomb_text);
        mClose = ViewUtils.findViewById(this, R.id.lucky_game_bomb_close);
        mClose.setOnClickListener(this);
    }

    private AnimatorSet getBombIconAnimation() {
        mBomb.setTranslationX(0.0f);
        mBomb.setScaleX(1.0f);
        mBomb.setScaleY(1.0f);
        mBomb.setAlpha(1.0f);

        int distance = CommonUtils.pxFromDp(10);
        ObjectAnimator trans = ObjectAnimator.ofFloat(mBomb, "translationX", 0, -distance);
        trans.setDuration(ICON_TRANSLATION_DURATION);
        trans.setRepeatCount(9);
        trans.setRepeatMode(ValueAnimator.REVERSE);

        ObjectAnimator transContinueL = ObjectAnimator.ofFloat(mBomb, "translationX", 0, -distance);
        transContinueL.setDuration(ICON_TRANSLATION_DURATION);

        ObjectAnimator transContinueR = ObjectAnimator.ofFloat(mBomb, "translationX", -distance, distance / 2);
        transContinueR.setDuration(ICON_TRANSLATION_DURATION);

        ObjectAnimator transContinueL2 = ObjectAnimator.ofFloat(mBomb, "translationX", distance / 2, -distance / 4);
        transContinueL2.setDuration(ICON_TRANSLATION_DURATION);

        ObjectAnimator transContinueL3 = ObjectAnimator.ofFloat(mBomb, "translationX", -distance / 4, -distance);
        transContinueL3.setDuration(ICON_TRANSLATION_DURATION);

        ObjectAnimator transContinueR2 = ObjectAnimator.ofFloat(mBomb, "translationX", -distance, 0);
        transContinueR2.setDuration(ICON_TRANSLATION_DURATION);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mBomb, "scaleX", 1.0f, 0.5f);
        scaleX.setDuration(ICON_DISAPPEAR_DURATION);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mBomb, "scaleY", 1.0f, 0.5f);
        scaleY.setDuration(ICON_DISAPPEAR_DURATION);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mBomb, "alpha", 1.0f, 0.0f);
        fadeOut.setDuration(ICON_DISAPPEAR_DURATION);

        AnimatorSet out = new AnimatorSet();
        out.playTogether(scaleX, scaleY, fadeOut);

        AnimatorSet bomb = new AnimatorSet();
        bomb.playSequentially(trans, transContinueL, transContinueR, transContinueL2, transContinueL3, transContinueR2, out);

        return bomb;
    }

    private AnimatorSet getEffectAnimation() {
        mEffect.setAlpha(0.0f);
        mEffect.setScaleX(0.4f);
        mEffect.setScaleY(0.4f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mEffect, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(EFFECT_FADE_IN_DURATION);

        ObjectAnimator enlargeEX = ObjectAnimator.ofFloat(mEffect, "scaleX", 0.4f, 1.0f);
        enlargeEX.setDuration(EFFECT_ENLARGE_DURATION);

        ObjectAnimator enlargeEY = ObjectAnimator.ofFloat(mEffect, "scaleY", 0.4f, 1.0f);
        enlargeEY.setDuration(EFFECT_ENLARGE_DURATION);

        AnimatorSet enlarge = new AnimatorSet();
        enlarge.playTogether(enlargeEX, enlargeEY);
        enlarge.setStartDelay(EFFECT_ENLARGE_DURATION);

        AnimatorSet effect = new AnimatorSet();
        effect.playTogether(fadeIn, enlarge);

        return effect;
    }

    private AnimatorSet getLightAnimation() {
        mLight.setAlpha(0.0f);
        mLight.setScaleX(0.4f);
        mLight.setScaleY(0.4f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mLight, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(1);

        ObjectAnimator enlargeX = ObjectAnimator.ofFloat(mLight, "scaleX", 0.4f, 1.0f);
        enlargeX.setDuration(LIGHT_ENLARGE_DURATION);

        ObjectAnimator enlargeY = ObjectAnimator.ofFloat(mLight, "scaleY", 0.4f, 1.0f);
        enlargeY.setDuration(LIGHT_ENLARGE_DURATION);

        AnimatorSet light = new AnimatorSet();
        light.playTogether(fadeIn, enlargeX, enlargeY);

        return light;
    }

    private AnimatorSet getTextAnimation() {
        mText.setAlpha(0.0f);
        mText.setScaleX(1.0f);
        mText.setScaleY(1.0f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mText, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(TEXT_FADE_IN_DURATION);

        ObjectAnimator enlargeX = ObjectAnimator.ofFloat(mText, "scaleX", 1.0f, 1.5f);
        enlargeX.setDuration(TEXT_FADE_IN_DURATION);

        ObjectAnimator enlargeY = ObjectAnimator.ofFloat(mText, "scaleY", 1.0f, 1.5f);
        enlargeY.setDuration(TEXT_FADE_IN_DURATION);

        AnimatorSet in = new AnimatorSet();
        in.playTogether(fadeIn, enlargeX, enlargeY);

        ObjectAnimator shrinkX = ObjectAnimator.ofFloat(mText, "scaleX", 1.5f, 1.0f);
        shrinkX.setDuration(TEXT_SHRINK_DURATION);

        ObjectAnimator shrinkY = ObjectAnimator.ofFloat(mText, "scaleY", 1.5f, 1.0f);
        shrinkY.setDuration(TEXT_SHRINK_DURATION);

        AnimatorSet shrink = new AnimatorSet();
        shrink.playTogether(shrinkX, shrinkY);

        AnimatorSet text = new AnimatorSet();
        text.playSequentially(in, shrink);

        return text;
    }

    private AnimatorSet getFragmentAnimation() {
        mFragment.setAlpha(0.0f);
        mFragment.setScaleX(0.4f);
        mFragment.setScaleY(0.4f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mFragment, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(FRAGMENT_ENLARGE_DURATION);

        ObjectAnimator enlargeX = ObjectAnimator.ofFloat(mFragment, "scaleX", 0.4f, 1.0f);
        enlargeX.setDuration(FRAGMENT_ENLARGE_DURATION);

        ObjectAnimator enlargeY = ObjectAnimator.ofFloat(mFragment, "scaleY", 0.4f, 1.0f);
        enlargeY.setDuration(FRAGMENT_ENLARGE_DURATION);

        AnimatorSet fragment = new AnimatorSet();
        fragment.playTogether(fadeIn, enlargeX, enlargeY);

        return fragment;
    }

    private AnimatorSet getFireAnimation() {
        mFire.setAlpha(0.0f);
        mFire.setScaleX(0.4f);
        mFire.setScaleY(0.4f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mFire, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(FRAGMENT_ENLARGE_DURATION);

        ObjectAnimator enlargeX = ObjectAnimator.ofFloat(mFire, "scaleX", 0.4f, 1.0f);
        enlargeX.setDuration(FRAGMENT_ENLARGE_DURATION);

        ObjectAnimator enlargeY = ObjectAnimator.ofFloat(mFire, "scaleY", 0.4f, 1.0f);
        enlargeY.setDuration(FRAGMENT_ENLARGE_DURATION);

        AnimatorSet fire = new AnimatorSet();
        fire.playTogether(fadeIn, enlargeX, enlargeY);

        return fire;

    }

    private ObjectAnimator getDescriptionAnimation() {
        mDescription.setAlpha(0.0f);
        String chanceCountString = String.format(Locale.US, "%03d", ((LuckyActivity) getContext()).getChanceCount());
        mDescription.setText((getContext().getString(R.string.lucky_game_award_boom_description)) + chanceCountString);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mDescription, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(DESCRIPTION_FADE_IN_DURATION);

        return fadeIn;
    }

    private ObjectAnimator getCloseAnimation() {
        mClose.setAlpha(0.0f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mClose, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(DESCRIPTION_FADE_IN_DURATION);
        return fadeIn;
    }

    public AnimatorSet getBombAnimation() {
        AnimatorSet bombIcon = getBombIconAnimation();
        AnimatorSet effect = getEffectAnimation();
        effect.setStartDelay(ICON_TRANSLATION_DURATION * 17);
        AnimatorSet light = getLightAnimation();
        light.setStartDelay(ICON_TRANSLATION_DURATION * 20);
        AnimatorSet text = getTextAnimation();
        text.setStartDelay(ICON_TRANSLATION_DURATION * 21);

        AnimatorSet fragment = getFragmentAnimation();
        fragment.setStartDelay(ICON_TRANSLATION_DURATION * 20);

        AnimatorSet fire = getFireAnimation();
        fire.setStartDelay(ICON_TRANSLATION_DURATION * 23);

        ObjectAnimator description = getDescriptionAnimation();
        description.setStartDelay(ICON_TRANSLATION_DURATION * 20);

        ObjectAnimator close = getCloseAnimation();
        close.setStartDelay(ICON_TRANSLATION_DURATION * 20);

        ObjectAnimator fragmentFadeOut = ObjectAnimator.ofFloat(mFragment, "alpha", 1.0f, 0.0f);
        fragmentFadeOut.setDuration(FRAGMENT_FADE_OUT_DURATION);
        fragmentFadeOut.setStartDelay(ICON_TRANSLATION_DURATION * 26);

        ObjectAnimator fireFadeOut = ObjectAnimator.ofFloat(mFire, "alpha", 1.0f, 0.0f);
        fireFadeOut.setDuration(FIRE_FADE_OUT_DURATION);
        fireFadeOut.setStartDelay(ICON_TRANSLATION_DURATION * 30);

        AnimatorSet bomb = new AnimatorSet();
        bomb.playTogether(bombIcon, effect, light, text, fragment, fire, description, close, fragmentFadeOut, fireFadeOut);

        return bomb;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lucky_game_bomb_close:
                ((LuckyActivity) getContext()).hideAwardView("Close");
                break;
        }
    }
}

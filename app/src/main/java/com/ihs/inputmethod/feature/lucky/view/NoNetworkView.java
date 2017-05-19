package com.ihs.inputmethod.feature.lucky.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.lucky.LuckyActivity;
import com.honeycomb.launcher.util.Utils;
import com.honeycomb.launcher.util.ViewUtils;


public class NoNetworkView extends FrameLayout implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private final long WIFI_BLINK_DURATION = 200;
    private final long DESCRIPTION_FADE_IN_DURATION = 133;
    private final long DESCRIPTION_SCALE_DURATION = 233;
    private final long FLAG_ENLARGE_DURATION = 67;
    private final long FLAG_SHRINK_DURATION = 100;
    private final long FLY_IN_DURATION = 200;
    private final long PLANET_SCALE_DURATION = 233;
    private final long PLANET_FADE_IN_DURATION = 100;
    private final long TRANSLATION_UP_DURATION = 200;

    private View mContainer;
    private View mDescription;
    private View mWifi;
    private View mFlag;

    public NoNetworkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContainer = ViewUtils.findViewById(this, R.id.lucky_game_no_network_container);
        mDescription = ViewUtils.findViewById(this, R.id.lucky_game_no_network_description);
        mFlag = ViewUtils.findViewById(this, R.id.lucky_game_no_network_flag);
        mWifi = ViewUtils.findViewById(this, R.id.lucky_game_no_network_wifi);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    private AnimatorSet getDescriptionAnimation() {
        mDescription.setScaleX(0.6f);
        mDescription.setScaleY(0.6f);
        mDescription.setAlpha(0.0f);

        ObjectAnimator scaleLargeX = ObjectAnimator.ofFloat(mDescription, "scaleX", 0.6f, 1.3f, 1.0f);
        scaleLargeX.setDuration(DESCRIPTION_SCALE_DURATION);

        ObjectAnimator scaleLargeY = ObjectAnimator.ofFloat(mDescription, "scaleY", 0.6f, 1.3f, 1.0f);
        scaleLargeY.setDuration(DESCRIPTION_SCALE_DURATION);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mDescription, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(DESCRIPTION_FADE_IN_DURATION);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fadeIn, scaleLargeX, scaleLargeY);
        set.setStartDelay(200);
        return set;
    }

    private AnimatorSet getFlagAnimation() {
        mDescription.setScaleX(1.0f);
        mDescription.setScaleY(1.0f);

        ObjectAnimator enlargeX = ObjectAnimator.ofFloat(mFlag, "scaleX", 1.0f, 1.5f);
        enlargeX.setDuration(FLAG_ENLARGE_DURATION );

        ObjectAnimator enlargeY = ObjectAnimator.ofFloat(mFlag, "scaleY", 1.0f, 1.5f);
        enlargeY.setDuration(FLAG_ENLARGE_DURATION);

        AnimatorSet enlarge = new AnimatorSet();
        enlarge.playTogether(enlargeX, enlargeY);

        ObjectAnimator shrinkX = ObjectAnimator.ofFloat(mFlag, "scaleX", 1.5f, 1.0f);
        shrinkX.setDuration(FLAG_SHRINK_DURATION);

        ObjectAnimator shrinkY = ObjectAnimator.ofFloat(mFlag, "scaleY", 1.5f, 1.0f);
        shrinkY.setDuration(FLAG_SHRINK_DURATION);

        AnimatorSet shrink = new AnimatorSet();
        shrink.playTogether(shrinkX, shrinkY);

        AnimatorSet scale = new AnimatorSet();
        scale.playSequentially(enlarge, shrink);
        return scale;
    }

    private ObjectAnimator getWifiAnimation() {
        ObjectAnimator blink = ObjectAnimator.ofFloat(mWifi, "alpha", 0.0f, 1.0f);
        blink.setDuration(WIFI_BLINK_DURATION);
        blink.setRepeatCount(ObjectAnimator.INFINITE);
        blink.setRepeatMode(ObjectAnimator.RESTART);
        return blink;
    }

    private AnimatorSet getContainerAnimation() {
        mContainer.setAlpha(0.0f);
        mContainer.setScaleX(0.2f);
        mContainer.setScaleY(0.2f);
        mContainer.setTranslationY(0);

        ObjectAnimator enlargeX = ObjectAnimator.ofFloat(mContainer, "scaleX", 0.2f, 1.0f);
        enlargeX.setDuration(FLY_IN_DURATION);

        ObjectAnimator enlargeY = ObjectAnimator.ofFloat(mContainer, "scaleY", 0.2f, 1.0f);
        enlargeY.setDuration(FLY_IN_DURATION);

        Point screenSize = Utils.getScreenSize((LuckyActivity) getContext());
        ObjectAnimator transY = ObjectAnimator.ofFloat(mContainer, "translationY", 0, -screenSize.y * 5 / 12);
        transY.setDuration(TRANSLATION_UP_DURATION);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mContainer, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(PLANET_FADE_IN_DURATION);

        AnimatorSet flyIn = new AnimatorSet();
        flyIn.playTogether(fadeIn, transY, enlargeX, enlargeY);

        return flyIn;
    }

    private AnimatorSet getPlanetScaleAnimation() {
        mContainer.setScaleX(0.2f);
        mContainer.setScaleY(0.2f);

        ObjectAnimator enlargeX = ObjectAnimator.ofFloat(mContainer, "scaleX", 1.0f, 1.2f, 1.0f);
        enlargeX.setDuration(PLANET_SCALE_DURATION);

        ObjectAnimator enlargeY = ObjectAnimator.ofFloat(mContainer, "scaleY", 1.0f, 1.2f, 1.0f);
        enlargeY.setDuration(PLANET_SCALE_DURATION);

        AnimatorSet enlarge = new AnimatorSet();
        enlarge.playTogether(enlargeX, enlargeY);

        return enlarge;
    }



    public AnimatorSet getNoNetworkAnimation() {
        AnimatorSet component = new AnimatorSet();
        component.playTogether(getFlagAnimation(), getWifiAnimation());

        AnimatorSet planet = new AnimatorSet();
        planet.playSequentially(getContainerAnimation(), getPlanetScaleAnimation(), component);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(planet, getDescriptionAnimation());
        return set;
    }
}

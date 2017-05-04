package com.ihs.inputmethod.uimodules.settings;

import android.content.Context;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ihs.inputmethod.uimodules.BaseFunction;

public class SettingsButtonView extends FrameLayout implements BaseFunction.NewTipStatueChangeListener {
    private final static int ANIMATION_DURATION = 300;

    private SettingsButton menuButton;
    private SettingsButton backButton;
    private View currentButton;

    private int buttonType = SettingsButton.SettingButtonType.MENU;

    public SettingsButtonView(Context context) {
        this(context, null);
    }

    public SettingsButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;

        menuButton = new SettingsButton(getContext());
        menuButton.setLayoutParams(lp);
        addView(menuButton);
        currentButton = menuButton;

        backButton = new SettingsButton(getContext());
        backButton.setLayoutParams(lp);
        backButton.setButtonType(SettingsButton.SettingButtonType.SETTING);
        backButton.setVisibility(View.INVISIBLE);
        addView(backButton);
    }

    public void doFunctionButtonSwitchAnimation() {
        menuButton.clearAnimation();
        backButton.clearAnimation();

        if (menuButton.isShown()) {
            doMenuButtonToBackButtonAnimation();
        } else {
            doBackButtonToMenuButtonAnimation();
        }
    }

    private void doMenuButtonToBackButtonAnimation() {
        backButton.setVisibility(View.VISIBLE);

        // Menu button animation
        RotateAnimation ra = new RotateAnimation(0.0f, 90.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(ANIMATION_DURATION);
        ra.setFillAfter(true);
        ra.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                menuButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
        aa.setDuration(ANIMATION_DURATION);
        aa.setFillAfter(true);

        AnimationSet as = new AnimationSet(true);
        as.addAnimation(ra);
        as.addAnimation(aa);
        as.setInterpolator(new FastOutSlowInInterpolator());

        // Back button animation
        RotateAnimation ra2 = new RotateAnimation(-90.0f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra2.setDuration(ANIMATION_DURATION);
        ra2.setFillAfter(true);

        AlphaAnimation aa2 = new AlphaAnimation(0.0f, 1.0f);
        aa2.setDuration(ANIMATION_DURATION);
        aa2.setFillAfter(true);

        AnimationSet as2 = new AnimationSet(true);
        as2.addAnimation(ra2);
        as2.addAnimation(aa2);
        as2.setInterpolator(new FastOutSlowInInterpolator());

        // Do animations
        menuButton.startAnimation(as);
        backButton.startAnimation(as2);
    }

    private void doBackButtonToMenuButtonAnimation() {
        menuButton.setVisibility(View.VISIBLE);

        // Menu button animation
        RotateAnimation ra = new RotateAnimation(90.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(ANIMATION_DURATION);
        ra.setFillAfter(true);

        AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
        aa.setDuration(ANIMATION_DURATION);
        aa.setFillAfter(true);

        AnimationSet as = new AnimationSet(true);
        as.addAnimation(ra);
        as.addAnimation(aa);
        as.setInterpolator(new FastOutSlowInInterpolator());

        // Back button animation
        RotateAnimation ra2 = new RotateAnimation(0.0f, -90.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra2.setDuration(ANIMATION_DURATION);
        ra2.setFillAfter(true);
        ra2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                backButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AlphaAnimation aa2 = new AlphaAnimation(1.0f, 0.0f);
        aa2.setDuration(ANIMATION_DURATION);
        aa2.setFillAfter(true);

        AnimationSet as2 = new AnimationSet(true);
        as2.addAnimation(ra2);
        as2.addAnimation(aa2);
        as2.setInterpolator(new FastOutSlowInInterpolator());

        // Do animations
        menuButton.startAnimation(as);
        backButton.startAnimation(as2);
    }

    public int getButtonType() {
        return buttonType;
    }

    public void setButtonType(int imageType) {
        if (this.buttonType == imageType) {
            return;
        }

        this.buttonType = imageType;
        refreshDrawable();
    }

    private void refreshDrawable() {
        ImageView iv;

        switch (buttonType) {
            case SettingsButton.SettingButtonType.MENU:
                iv = menuButton;
                break;

            case SettingsButton.SettingButtonType.BACK:
            case SettingsButton.SettingButtonType.SETTING:
                iv = backButton;
                break;

            default:
                throw new IllegalArgumentException("set setting button type wrong !");
        }

        currentButton.setVisibility(View.INVISIBLE);
        iv.setVisibility(View.VISIBLE);
        currentButton = iv;
    }

    @Override
    public boolean shouldShowTip() {
        return buttonType == SettingsButton.SettingButtonType.MENU;
    }
}

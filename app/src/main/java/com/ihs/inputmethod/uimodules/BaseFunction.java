package com.ihs.inputmethod.uimodules;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.settings.SettingsButtonView;

import static com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils.getTransparentRippleBackground;

/**
 * Created by jixiang on 16/12/12.
 */
public class BaseFunction extends FrameLayout {
    private View functionView;
    private View functionBackView;
    private View newTipView;
    private NewTipStatueChangeListener newTipStatueChangeListener;
    private boolean showNewMark;
    private final static int FUNCTION_VIEW_REAL_WIDTH = 18; // function real width 18dp
    private final static int FUNCTION_VIEW_MARGIN_LEFT = 15; //margin value,unit dp
    private final static int ANIMATION_DURATION = 200;

    public BaseFunction(Context context) {
        super(context);
        this.setBackgroundDrawable(getTransparentRippleBackground());
    }

    public void setFunctionView(View view) {
        if (this.functionView != null) {
            removeView(this.functionView);
        }
        this.functionView = view;
        addView(functionView, 0);
    }

    public void setFunctionBackView(View view) {
        if (this.functionBackView != null) {
            removeView(this.functionBackView);
        }
        this.functionBackView = view;
        addView(functionBackView, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        updateNewTipPosition();
    }

    public void showNewTip() {
        if (newTipView == null) {
            newTipView = new View(HSApplication.getContext());
            GradientDrawable redPointDrawable = new GradientDrawable();
            redPointDrawable.setColor(Color.RED);
            redPointDrawable.setShape(GradientDrawable.OVAL);
            newTipView.setBackgroundDrawable(redPointDrawable);

            int width = HSDisplayUtils.dip2px(7);
            int height = HSDisplayUtils.dip2px(7);
            LayoutParams layoutParams = new LayoutParams(width, height);
            int leftMargin = HSDisplayUtils.dip2px(FUNCTION_VIEW_REAL_WIDTH) / 2;
            int bottomMargin = leftMargin;
            layoutParams.setMargins(leftMargin, 0, 0, bottomMargin);
            layoutParams.gravity = Gravity.CENTER;
            newTipView.setLayoutParams(layoutParams);
            addView(newTipView);
        }
        showNewMark = true;
    }

    private void updateNewTipPosition() {
        if (functionView != null) {
            int expectedWidth = (HSDisplayUtils.dip2px(FUNCTION_VIEW_REAL_WIDTH) + HSDisplayUtils.dip2px(FUNCTION_VIEW_MARGIN_LEFT) * 2);
            int horiMargin = (expectedWidth - functionView.getMeasuredWidth()) / 2;
            if (horiMargin > 0) {
                ((LayoutParams) functionView.getLayoutParams()).setMargins(horiMargin, 0, horiMargin, 0);
            }
        }
        if (newTipView != null && showNewMark) {
            newTipView.setVisibility(newTipStatueChangeListener.shouldShowTip() ? VISIBLE : GONE);
        }
    }

    public void hideNewTip() {
        if (newTipView != null) {
            removeView(newTipView);
            newTipView.setVisibility(GONE);
            newTipView = null;
        }
        showNewMark = false;
    }

    public void setNewTipStatueChangeListener(NewTipStatueChangeListener newTipStatueChangeListener) {
        this.newTipStatueChangeListener = newTipStatueChangeListener;
    }

    public interface NewTipStatueChangeListener {
        boolean shouldShowTip();
    }

    private boolean isMenuButtonShown() {
        return functionView.isShown();
    }

    public void doFunctionButtonSwitchAnimation() {
//        functionView.clearAnimation();
//        functionBackView.clearAnimation();
//
//        if (isMenuButtonShown()) {
//            doMenuButtonToBackButtonAnimation();
//        } else {
//            doBackButtonToMenuButtonAnimation();
//        }
        SettingsButtonView settingsButtonView = (SettingsButtonView) functionView;
        settingsButtonView.doFunctionButtonSwitchAnimation();
    }

    private void doMenuButtonToBackButtonAnimation() {
        functionBackView.setVisibility(View.VISIBLE);

        RotateAnimation ra = new RotateAnimation(0.0f, 90.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(ANIMATION_DURATION);
        ra.setFillAfter(true);
        ra.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                functionView.setVisibility(View.INVISIBLE);
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


        RotateAnimation ra2 = new RotateAnimation(-90.0f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra2.setDuration(ANIMATION_DURATION);
        ra2.setFillAfter(true);

        AlphaAnimation aa2 = new AlphaAnimation(0.0f, 1.0f);
        aa2.setDuration(ANIMATION_DURATION);
        aa2.setFillAfter(true);

        AnimationSet as2 = new AnimationSet(true);
        as2.addAnimation(ra2);
        as2.addAnimation(aa2);


        functionView.startAnimation(as);
        functionBackView.startAnimation(as2);

    }

    private void doBackButtonToMenuButtonAnimation() {
        functionView.setVisibility(View.VISIBLE);

        RotateAnimation ra = new RotateAnimation(90.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(ANIMATION_DURATION);
        ra.setFillAfter(true);

        AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
        aa.setDuration(ANIMATION_DURATION);
        aa.setFillAfter(true);

        AnimationSet as = new AnimationSet(true);
        as.addAnimation(ra);
        as.addAnimation(aa);



        RotateAnimation ra2 = new RotateAnimation(0.0f, -90.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra2.setDuration(ANIMATION_DURATION);
        ra2.setFillAfter(true);
        ra2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                functionBackView.setVisibility(View.INVISIBLE);
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

        functionView.startAnimation(as);
        functionBackView.startAnimation(as2);
    }

}

package com.ihs.inputmethod.uimodules.settings;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
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

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;

import static com.ihs.inputmethod.uimodules.BaseFunctionBar.getFuncButtonDrawable;

/**
 * Created by jixiang on 16/12/12.
 */
public class SettingsButtonView extends FrameLayout {
    private View newTipView;
    private NewTipStatueChangeListener newTipStatueChangeListener;
    private boolean showNewMark;
    private final static int FUNCTION_VIEW_REAL_WIDTH = 18; // function real width 18dp
    private final static int FUNCTION_VIEW_MARGIN_LEFT = 15; //margin value,unit dp
    private final static int ANIMATION_DURATION = 200;
    private static final String MENU_DRAWABLE = HSKeyboardThemeManager.IMG_MENU_FUNCTION;
    private static final String BACK_DRAWABLE = HSKeyboardThemeManager.IMG_MENU_BACK;

    private SettingsButton menuButton;
    private SettingsButton backButton;
    private View currentButton;

    private int buttonType = SettingsButton.SettingButtonType.MENU;

    public SettingsButtonView(Context context) {
        this(context, null);
        //this.setBackgroundDrawable(getTransparentRippleBackground());

    }

    public SettingsButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        menuButton = new SettingsButton(getContext());
        Drawable drawable = HSKeyboardThemeManager.getThemeSettingMenuDrawable(MENU_DRAWABLE, null);
        if (drawable == null) {
            drawable = VectorDrawableCompat.create(getResources(), getDrawableFromResources(MENU_DRAWABLE), null);
            drawable = getTintDrawable(drawable);
        }
        menuButton.setImageDrawable(drawable);
        addView(menuButton);
        currentButton = menuButton;

        backButton = new SettingsButton(getContext());
        drawable = HSKeyboardThemeManager.getThemeSettingMenuDrawable(BACK_DRAWABLE, null);
        if (drawable == null) {
            drawable = VectorDrawableCompat.create(getResources(), getDrawableFromResources(BACK_DRAWABLE), null);
            drawable = getTintDrawable(drawable);
        }
        backButton.setImageDrawable(drawable);
        backButton.setButtonType(SettingsButton.SettingButtonType.SETTING);
        backButton.setVisibility(View.INVISIBLE);
        addView(backButton);

        final FrameLayout.LayoutParams llp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llp.gravity = Gravity.CENTER;
        menuButton.setLayoutParams(llp);
        backButton.setLayoutParams(llp);

        //refreshDrawable();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();



        HSLog.d("testtest", "onFinishInflate5");
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
        return menuButton.isShown();
    }

    public void doFunctionButtonSwitchAnimation() {
        menuButton.clearAnimation();
        backButton.clearAnimation();

        if (isMenuButtonShown()) {
            doMenuButtonToBackButtonAnimation();
        } else {
            doBackButtonToMenuButtonAnimation();
        }
    }

    private void doMenuButtonToBackButtonAnimation() {
        backButton.setVisibility(View.VISIBLE);

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


        RotateAnimation ra2 = new RotateAnimation(-90.0f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra2.setDuration(ANIMATION_DURATION);
        ra2.setFillAfter(true);

        AlphaAnimation aa2 = new AlphaAnimation(0.0f, 1.0f);
        aa2.setDuration(ANIMATION_DURATION);
        aa2.setFillAfter(true);

        AnimationSet as2 = new AnimationSet(true);
        as2.addAnimation(ra2);
        as2.addAnimation(aa2);


        menuButton.startAnimation(as);
        backButton.startAnimation(as2);

    }

    private void doBackButtonToMenuButtonAnimation() {
        menuButton.setVisibility(View.VISIBLE);

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

        int oldType = this.buttonType;

        this.buttonType = imageType;
        refreshDrawable();

//        if (oldType != SettingsButton.SettingButtonType.BACK && this.buttonType != SettingsButton.SettingButtonType.BACK) {
//            doFunctionButtonSwitchAnimation();
//        }



//        if (this.buttonType == SettingsButton.SettingButtonType.MENU
//                || this.buttonType == SettingsButton.SettingButtonType.SETTING) {
//            doFunctionButtonSwitchAnimation();
//        }
    }

    private void refreshDrawable() {
        String drawableName;
        ImageView iv;

        switch (buttonType) {
            case SettingsButton.SettingButtonType.MENU:
                drawableName = MENU_DRAWABLE;
                iv = menuButton;
                break;

            case SettingsButton.SettingButtonType.BACK:
            case SettingsButton.SettingButtonType.SETTING:
                drawableName = BACK_DRAWABLE;
                iv = backButton;
                break;

            default:
                throw new IllegalArgumentException("set setting button type wrong !");
        }


//        Drawable drawable = HSKeyboardThemeManager.getThemeSettingMenuDrawable(drawableName, null);
//        if (drawable == null) {
//            drawable = VectorDrawableCompat.create(getResources(), getDrawableFromResources(drawableName), null);
//            drawable = getTintDrawable(drawable);
//        }

        currentButton.setVisibility(View.INVISIBLE);
        //iv.setImageDrawable(drawable);
        iv.setVisibility(View.VISIBLE);
        currentButton = iv;
    }

    @NonNull
    private Drawable getTintDrawable(Drawable drawable) {
        return getFuncButtonDrawable(drawable);
    }

    public int getDrawableFromResources(String resName) {
        if (resName.contains(".")) {
            resName = resName.substring(0, resName.indexOf("."));
        }
        return HSApplication.getContext().getResources().getIdentifier(resName, "drawable", HSApplication.getContext().getPackageName());
    }
}

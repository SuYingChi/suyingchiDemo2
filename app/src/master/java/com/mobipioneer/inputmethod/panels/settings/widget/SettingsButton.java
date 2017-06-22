package com.mobipioneer.inputmethod.panels.settings.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.panelcontainer.BasePanel;
import com.mobipioneer.lockerkeyboard.utils.MasterConstants;

/**
 * Created by chenyuanming on 12/10/2016.
 */

public class SettingsButton extends ActionButton {
    private boolean isSelected;
    private BasePanel panel;

    public SettingsButton(Context context) {
        super(context);
    }

    public SettingsButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingsButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private static final String PRESSED_DRAWABLE = "menu_back.png";

    private static final String NORMAL_DRAWABLE = "menu_setting.png";

    @Override
    protected Drawable getPressedDrawable() {
        return HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources(PRESSED_DRAWABLE);
    }

    @Override
    protected Drawable getNormalDrawable() {
        return HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources(NORMAL_DRAWABLE);
    }

    @Override
    protected OnClickListener getOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelected = !isSelected;
                setSelected(isSelected);
            }
        };
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        isSelected = selected;
        onButtonStateChanged(selected);
    }

    @Override
    public BasePanel getPanel() {
        if (panel == null) {
//            panel = KeyboardPluginManager.getInstance().getPanel(HSApplication.getContext().getString(R.string.panel_settings));
//            panel.show();
        }
        return panel;
    }

    public void onButtonStateChanged(boolean isSelected) {
        int animDuration = 300;
        View panelView = getPanel().getPanelView();
        if (panelView != null) {
            if (isSelected) {
                expandOrCollapse(panelView, true, animDuration);
                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(MasterConstants.GA_APP_KEYBOARD_SETTINGS_CLICK);
            } else {
                expandOrCollapse(panelView, false, animDuration);
            }
        }
    }

    private void expandOrCollapse(final View v, final boolean isExpand, final long animDuration) {
        if (v == null) {
            return;
        }
        float keyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources());
        ValueAnimator animator = isExpand ? ValueAnimator.ofFloat(0, keyboardHeight) : ValueAnimator.ofFloat(keyboardHeight, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(animDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float val = (float) animation.getAnimatedValue();
                setViewHeight(v, (int) val);
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isExpand) {
                    v.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }


    public void setViewHeight(View v, int height) {
        if (v != null && v.getLayoutParams() != null) {
            final ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = height;
            v.requestLayout();
        }
    }
}

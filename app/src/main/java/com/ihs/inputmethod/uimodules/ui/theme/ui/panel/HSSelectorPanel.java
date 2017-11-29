package com.ihs.inputmethod.uimodules.ui.theme.ui.panel;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.BaseFunctionBar;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.panelcontainer.BasePanel;

/**
 * Created by yanxia on 2017/11/28.
 */

public class HSSelectorPanel extends BasePanel {

    private ImageView selectorDirectionUp;
    private ImageView selectorDirectionDown;
    private ImageView selectorDirectionLeft;
    private ImageView selectorDirectionRight;
    private ImageView selectorDirectionSelect;

    @Override
    protected View onCreatePanelView() {
        //set functionBar setting button type
        FrameLayout panelView = new FrameLayout(HSApplication.getContext());
        BaseFunctionBar functionBar = (BaseFunctionBar) panelActionListener.getBarView();
        functionBar.setSettingButtonType(SettingsButton.SettingButtonType.BACK);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.settings_selector_layout, null);
        initView(view);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) HSApplication.getContext().getResources().getDimension(R.dimen.config_default_keyboard_height));
        panelView.addView(view, layoutParams);
        return panelView;
    }

    private void initView(View selectorView) {
        selectorView.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
        selectorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        selectorDirectionUp = selectorView.findViewById(R.id.selector_direction_up);
        selectorDirectionUp.setImageDrawable(VectorDrawableCompat.create(HSApplication.getContext().getResources(), R.drawable.ic_selector_arrow_top, null));
        selectorDirectionDown = selectorView.findViewById(R.id.selector_direction_down);
        selectorDirectionDown.setImageDrawable(VectorDrawableCompat.create(HSApplication.getContext().getResources(), R.drawable.ic_selector_arrow_top, null));
        selectorDirectionLeft = selectorView.findViewById(R.id.selector_direction_left);
        selectorDirectionLeft.setImageDrawable(VectorDrawableCompat.create(HSApplication.getContext().getResources(), R.drawable.ic_selector_arrow_top, null));
        selectorDirectionRight = selectorView.findViewById(R.id.selector_direction_right);
        selectorDirectionRight.setImageDrawable(VectorDrawableCompat.create(HSApplication.getContext().getResources(), R.drawable.ic_selector_arrow_top, null));
        selectorDirectionSelect = selectorView.findViewById(R.id.selector_select);
        selectorDirectionSelect.setImageDrawable(VectorDrawableCompat.create(HSApplication.getContext().getResources(), R.drawable.ic_selector, null));

        if (HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            selectorDirectionUp.setBackgroundResource(R.drawable.settings_key_common_background_selector);
            selectorDirectionDown.setBackgroundResource(R.drawable.settings_key_common_background_selector);
            selectorDirectionLeft.setBackgroundResource(R.drawable.settings_key_common_background_selector);
            selectorDirectionRight.setBackgroundResource(R.drawable.settings_key_common_background_selector);
            selectorDirectionSelect.setBackgroundResource(R.drawable.settings_key_common_background_selector);
        } else {
            selectorDirectionUp.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
            selectorDirectionDown.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
            selectorDirectionLeft.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
            selectorDirectionRight.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
            selectorDirectionSelect.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
        }
    }

    private Drawable getBgDrawable() {
        StateListDrawable stateListDrawable = new StateListDrawable();
        return stateListDrawable;
    }

    public HSSelectorPanel() {
        super();
    }

    @Override
    protected boolean onHidePanelView(int appearMode) {
        return super.onHidePanelView(appearMode);
    }

    @Override
    protected boolean onShowPanelView(int appearMode) {
        return super.onShowPanelView(appearMode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View getPanelView() {
        return super.getPanelView();
    }

    @Override
    public void setPanelView(View rootView) {
        super.setPanelView(rootView);
    }

    @Override
    public void showChildPanel(Class panelClass, Bundle bundle) {
        super.showChildPanel(panelClass, bundle);
    }

    @Override
    protected Bundle getBundle() {
        return super.getBundle();
    }

    @Override
    public void setBarVisibility(int visibility) {
        super.setBarVisibility(visibility);
    }

    @Override
    public View getKeyboardView() {
        return super.getKeyboardView();
    }

    @Override
    public Animation getAppearAnimator() {
        return super.getAppearAnimator();
    }

    @Override
    public Animation getDismissAnimator() {
        return super.getDismissAnimator();
    }

    @Override
    public void setOnAnimationListener(OnAnimationListener onAnimationListener) {
        super.setOnAnimationListener(onAnimationListener);
    }

    @Override
    public void setPanelActionListener(OnPanelActionListener onPanelActionListener) {
        super.setPanelActionListener(onPanelActionListener);
    }

    @Override
    public OnPanelActionListener getPanelActionListener() {
        return super.getPanelActionListener();
    }

    @Override
    public void backToParentPanel(boolean keepSelf) {
        super.backToParentPanel(keepSelf);
    }
}

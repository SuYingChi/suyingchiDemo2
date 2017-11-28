package com.ihs.inputmethod.uimodules.ui.theme.ui.panel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.BaseFunctionBar;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.panelcontainer.BasePanel;

/**
 * Created by yanxia on 2017/11/28.
 */

public class HSSelectorPanel extends BasePanel {
    @Override
    protected View onCreatePanelView() {
        //set functionBar setting button type
        BaseFunctionBar functionBar = (BaseFunctionBar) panelActionListener.getBarView();
        functionBar.setSettingButtonType(SettingsButton.SettingButtonType.BACK);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.settings_selector_layout, null);
        return view;
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

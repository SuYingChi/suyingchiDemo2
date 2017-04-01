package com.ihs.inputmethod.uimodules;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.settings.HSNewSettingsPanel;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.inputmethod.uimodules.ui.emoticon.HSEmoticonPanel;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.uimodules.widget.videoview.HSMediaView;
import com.ihs.inputmethod.view.KBImageView;
import com.ihs.inputmethod.websearch.WebContentSearchManager;
import com.ihs.panelcontainer.KeyboardPanelSwitchContainer;
import com.ihs.panelcontainer.KeyboardPanelSwitcher;
import com.ihs.panelcontainer.panel.KeyboardPanel;

import static android.view.View.GONE;

/**
 * Created by jixiang on 16/11/17.
 */

public class KeyboardPanelManager extends KeyboardPanelSwitcher implements BaseFunctionBar.OnFunctionBarItemClickListener {

    public KeyboardPanelManager() {
    }


    private KeyboardPanelSwitchContainer keyboardPanelSwitchContainer;
    private BaseFunctionBar functionBar;
    private HSMediaView hsBackgroundVedioView;


    private INotificationObserver notificationObserver = new INotificationObserver() {

        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED.equals(s)) {
                addOrUpdateBackgroundView();
            } else if (HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD.equals(s)) {
                showKeyboardWithMenu();
                functionBar.showNewMarkIfNeed();
            }
        }
    };

    private void addOrUpdateBackgroundView() {
        //set bar layout background
        Context context = HSApplication.getContext();
        if (keyboardPanelSwitchContainer != null) {
            KBImageView barBottomView = keyboardPanelSwitchContainer.getBarBottomView();

            Drawable drawable = HSKeyboardThemeManager.getSuggestionBackgroundDrawable();

            int height = context.getResources().getDimensionPixelOffset(R.dimen.config_suggestions_strip_height);
            int width = HSResourceUtils.getDefaultKeyboardWidth(context.getResources());
            KBImageView imageView = null;
            ViewGroup barViewGroup = keyboardPanelSwitchContainer.getBarViewGroup();
            String TAG_TILED_VIEW = "TAG_TILED_VIEW";
            for (int i = 0; i < barViewGroup.getChildCount(); i++) {
                if (TAG_TILED_VIEW.equals(barViewGroup.getChildAt(i).getTag())) {
                    imageView = (KBImageView) barViewGroup.getChildAt(i);
                    break;
                }
            }
            if (imageView == null) {
                imageView = new KBImageView(context);
                imageView.setTag(TAG_TILED_VIEW);
                barViewGroup.addView(imageView, 0);

                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
                layoutParams.gravity = Gravity.CENTER;

                imageView.setLayoutParams(layoutParams);
            }
            imageView.setBackgroundDrawable(drawable);
            barBottomView.setBottomBackgroundDrawable(drawable, width);

        }
    }


    public View onCreateInputView(View keyboardPanelView) {
        onInputViewDestroy();
        keyboardPanelSwitchContainer = new KeyboardPanelSwitchContainer();
        //todo 改为东哥backgroundView
//        keyboardPanelSwitchContainer.setThemeBackground(HSKeyboardThemeManager.getKeyboardBackgroundDrawable());
        ThemeAnalyticsReporter.getInstance().recordThemeUsage(HSKeyboardThemeManager.getCurrentThemeName());
        hsBackgroundVedioView = new HSMediaView(HSApplication.getContext());
        hsBackgroundVedioView.setTag("BackgroundView");
        hsBackgroundVedioView.setSupportSmoothScroll(false);
        hsBackgroundVedioView.init();
        keyboardPanelSwitchContainer.setBackgroundView(hsBackgroundVedioView);
        keyboardPanelSwitchContainer.setKeyboardPanel(KeyboardPanel.class, keyboardPanelView);
        keyboardPanelSwitchContainer.setWhitePanel(HSNewSettingsPanel.class);

        keyboardPanelSwitchContainer.setWebHistoryView(WebContentSearchManager.getInstance().getWebSearchHistoryView());

        createDefaultFunctionBar();
        setFunctionBar(functionBar);
        keyboardPanelSwitchContainer.showPanel(KeyboardPanel.class);

        addOrUpdateBackgroundView();

        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD, notificationObserver);
        return keyboardPanelSwitchContainer;
    }

    public void initInputView(View keyboardPanelView) {
        keyboardPanelSwitchContainer = (KeyboardPanelSwitchContainer) keyboardPanelView;
        hsBackgroundVedioView = (HSMediaView) keyboardPanelSwitchContainer.findViewWithTag("BackgroundView");
        ((ViewGroup)hsBackgroundVedioView.getParent()).removeView(hsBackgroundVedioView);
        keyboardPanelSwitchContainer.setBackgroundView(hsBackgroundVedioView);

        View keyboardView = keyboardPanelView.findViewById(R.id.input_view);
        ((ViewGroup)keyboardView.getParent()).removeView(keyboardView);
        keyboardPanelSwitchContainer.setKeyboardPanel(KeyboardPanel.class, keyboardView);

        functionBar = (BaseFunctionBar) keyboardPanelView.findViewById(R.id.function_layout);
        ((ViewGroup)functionBar.getParent()).removeView(functionBar);
        setFunctionBar(functionBar);
        keyboardPanelSwitchContainer.showPanel(KeyboardPanel.class);

        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD, notificationObserver);
    }



    private void createDefaultFunctionBar() {
        functionBar = (BaseFunctionBar) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.base_funtion_bar, null);
    }

    private void setFunctionBar(BaseFunctionBar newFunctionBar) {
        this.functionBar = newFunctionBar;
        if (this.functionBar != null) {
            keyboardPanelSwitchContainer.setBarView(this.functionBar);
            newFunctionBar.setOnFunctionBarClickListener(this);
//            keyboardPanelSwitchContainer.getBarViewGroup().setBackgroundDrawable(KeyboardThemeManager.getCurrentTheme().getSuggestionBackground(new ColorDrawable(Color.TRANSPARENT)));
            final int height = HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.config_suggestions_strip_height);
            ViewGroup.LayoutParams layoutParams = keyboardPanelSwitchContainer.getBarViewGroup().getLayoutParams();
            layoutParams.height = height;
            keyboardPanelSwitchContainer.getBarViewGroup().setLayoutParams(layoutParams);
        }
    }

    public void onInputViewDestroy() {
        if (keyboardPanelSwitchContainer != null) {
            keyboardPanelSwitchContainer.onDestroy();
            keyboardPanelSwitchContainer = null;
        }

        if (functionBar != null) {
            functionBar.onDestroy();
            functionBar = null;
        }

        HSGlobalNotificationCenter.removeObserver(notificationObserver);
    }

    @Override
    public void onFunctionBarItemClick(View view) {
        if (view.getId() == R.id.func_setting_button) {
            int settingButtonType = functionBar.getSettingButtonType();
            switch (settingButtonType) {
                case SettingsButton.SettingButtonType.MENU:
                    keyboardPanelSwitchContainer.showChildPanel(HSNewSettingsPanel.class, null);
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_function_button_click");
                    break;

                case SettingsButton.SettingButtonType.SETTING:
                    keyboardPanelSwitchContainer.backToParentPanel(false);
                    break;

                case SettingsButton.SettingButtonType.BACK:
                    keyboardPanelSwitchContainer.backToParentPanel(false);
                    if (keyboardPanelSwitchContainer.getCurrentPanel() == keyboardPanelSwitchContainer.getKeyboardPanel()) {
                        functionBar.setSettingButtonType(SettingsButton.SettingButtonType.MENU);
                    } else {
                        functionBar.setSettingButtonType(SettingsButton.SettingButtonType.BACK);
                    }
                    break;
            }
        }

        if (view.getId() == R.id.web_search_icon) {
            keyboardPanelSwitchContainer.getKeyboardPanel().switchSuggestionState(KeyboardPanel.SUGGESTION_WEB_HISTORY);
        }

        if (view.getId() == R.id.func_cloth_button) {
            Intent intent = new Intent(HSApplication.getContext(), ThemeHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            HSApplication.getContext().startActivity(intent);
            HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_cloth_button_click");
        }
    }

    public void showKeyboardWithMenu() {
        if (keyboardPanelSwitchContainer != null) {
            keyboardPanelSwitchContainer.showPanel(KeyboardPanel.class);
        }
        if (functionBar != null) {
            functionBar.setSettingButtonType(SettingsButton.SettingButtonType.MENU);
        }
    }

    public void showEmojiPanel() {
        keyboardPanelSwitchContainer.showPanel(HSEmoticonPanel.class);
        keyboardPanelSwitchContainer.setBarVisibility(GONE);
    }

    public void beforeStartInputView() {

        if (hsBackgroundVedioView != null) {
            hsBackgroundVedioView.setHSBackground(HSKeyboardThemeManager.getCurrentThemeBackgroundPath());
        }
    }

    public void onBackPressed() {
        if (hsBackgroundVedioView != null) {
            hsBackgroundVedioView.stopHSMedia();
        }
    }

    public void onHomePressed() {
        if (hsBackgroundVedioView != null) {
            hsBackgroundVedioView.stopHSMedia();
        }
    }

    public void resetKeyboardBarState() {
        if (keyboardPanelSwitchContainer != null) {
            keyboardPanelSwitchContainer.getKeyboardPanel().switchSuggestionState(0);
            keyboardPanelSwitchContainer.getBarViewGroup().setVisibility(View.VISIBLE);
        }
    }

}

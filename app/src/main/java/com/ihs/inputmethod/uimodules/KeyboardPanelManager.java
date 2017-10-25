package com.ihs.inputmethod.uimodules;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.acb.adadapter.AcbAd;
import com.acb.adadapter.AcbNativeAd;
import com.acb.nativeads.AcbNativeAdLoader;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.chargingscreen.utils.FeatureDelayReleaseUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.adpanel.KeyboardPanelAdManager;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.framework.KeyboardSwitcher;
import com.ihs.inputmethod.uimodules.settings.HSNewSettingsPanel;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.inputmethod.uimodules.ui.emoticon.HSEmoticonActionBar;
import com.ihs.inputmethod.uimodules.ui.emoticon.HSEmoticonPanel;
import com.ihs.inputmethod.uimodules.ui.sticker.Sticker;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerSuggestionAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.uimodules.widget.bannerad.KeyboardBannerAdLayout;
import com.ihs.inputmethod.uimodules.widget.goolgeplayad.CustomBarGPAdAdapter;
import com.ihs.inputmethod.uimodules.widget.goolgeplayad.CustomizeBarLayout;
import com.ihs.inputmethod.uimodules.widget.videoview.HSMediaView;
import com.ihs.inputmethod.view.KBImageView;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.utils.KCFeatureRestrictionConfig;
import com.ihs.panelcontainer.KeyboardPanelSwitchContainer;
import com.ihs.panelcontainer.KeyboardPanelSwitcher;
import com.ihs.panelcontainer.panel.KeyboardPanel;
import com.keyboard.core.session.KCKeyboardSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.view.Surface.ROTATION_0;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class KeyboardPanelManager extends KeyboardPanelSwitcher implements BaseFunctionBar.OnFunctionBarItemClickListener {


    public KeyboardPanelManager() {
    }


    private KeyboardPanelSwitchContainer keyboardPanelSwitchContainer;
    private BaseFunctionBar functionBar;
    private HSMediaView hsBackgroundVedioView;
    private List<AcbNativeAd> gpNativeAdList = new ArrayList<>();
    private CustomBarGPAdAdapter gpAdAdapter;
    private AcbNativeAdLoader acbNativeAdLoader;
    private RecyclerView gpAdRecyclerView;
    private KeyboardPanelAdManager keyboardPanelAdManager;
    private List<Integer> bannerAdSessionList;
    private List<Map<String, Object>> cameraAdInfoList;
    private Random random = new Random();


    private INotificationObserver notificationObserver = new INotificationObserver() {

        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED.equals(s)) {
                addOrUpdateBackgroundView();
            } else if (HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD.equals(s)) {
                showKeyboardWithMenu();
                functionBar.showNewMarkIfNeed();
                functionBar.showMakeFacemojiTipIfNeed(keyboardPanelSwitchContainer);
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
        bannerAdSessionList = (List<Integer>) HSConfig.getList("Application", "NativeAds", "KeyboardBannerAd", "SessionIndexOfDay");
        keyboardPanelSwitchContainer = new KeyboardPanelSwitchContainer();
        //todo 改为东哥backgroundView
//        keyboardPanelSwitchContainer.setThemeBackground(HSKeyboardThemeManager.getKeyboardBackgroundDrawable());
        ThemeAnalyticsReporter.getInstance().recordThemeUsage(HSKeyboardThemeManager.getCurrentThemeName());
        hsBackgroundVedioView = new HSMediaView(HSApplication.getContext());
        hsBackgroundVedioView.setTag("BackgroundView");
        hsBackgroundVedioView.setSupportSmoothScroll(false);
        hsBackgroundVedioView.init();
        keyboardPanelSwitchContainer.setBackgroundView(hsBackgroundVedioView);
        keyboardPanelSwitchContainer.setWhitePanel(HSNewSettingsPanel.class);
        keyboardPanelAdManager = new KeyboardPanelAdManager("FunctionBarGiftAd");

        createDefaultFunctionBar();
        setFunctionBar(functionBar);
        addOrUpdateBackgroundView();

        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD, notificationObserver);

        return keyboardPanelSwitchContainer;
    }

    @Override
    public void showKeyboardPanel() {
        keyboardPanelSwitchContainer.setKeyboardPanel(KeyboardPanel.class, KeyboardSwitcher.getInstance().getKeyboardPanelView());
        keyboardPanelSwitchContainer.showPanel(KeyboardPanel.class);
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
            SettingsButton settingsButton = functionBar.getSettingsButton();
            int settingButtonType = settingsButton.getButtonType();

            switch (settingButtonType) {
                case SettingsButton.SettingButtonType.MENU:
                    settingsButton.doFunctionButtonSwitchAnimation();
                    keyboardPanelSwitchContainer.showChildPanel(HSNewSettingsPanel.class, null);
                    HSAnalytics.logEvent("keyboard_function_button_click");
                    break;

                case SettingsButton.SettingButtonType.SETTING:
                    settingsButton.doFunctionButtonSwitchAnimation();
                    keyboardPanelSwitchContainer.backToParentPanel(false);
                    break;

                case SettingsButton.SettingButtonType.BACK:
                    keyboardPanelSwitchContainer.backToParentPanel(false);
                    if (keyboardPanelSwitchContainer.getCurrentPanel() == keyboardPanelSwitchContainer.getKeyboardPanel()) {
                        functionBar.setSettingButtonType(SettingsButton.SettingButtonType.MENU);
                    } else {
                        functionBar.setSettingButtonType(SettingsButton.SettingButtonType.SETTING);
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
            intent.putExtra("From", "Keyboard");
            HSApplication.getContext().startActivity(intent);
            HSAnalytics.logEvent("keyboard_cloth_button_click");
        }

        if (view.getId() == R.id.func_facemoji_button){
            HSEmoticonActionBar.saveLastPanelName(HSEmoticonActionBar.PANEL_FACEEMOJI);
            keyboardPanelSwitchContainer.showPanelAndKeepSelf(HSEmoticonPanel.class);
            keyboardPanelSwitchContainer.setBarVisibility(GONE);
            functionBar.dismissMakeFacemojiTip();
        }
    }

    public void showKeyboardWithMenu() {
        if (keyboardPanelSwitchContainer != null) {
            showKeyboardPanel();
        }
        if (functionBar != null) {
            functionBar.setSettingButtonType(SettingsButton.SettingButtonType.MENU);
        }
    }

    public void showFunctionBarAd() {
        if (keyboardPanelAdManager.isShowAdConditionSatisfied()) {
            if (functionBar.startFunctionBarAdAnimation()) {
                keyboardPanelAdManager.hasShowedAd();
            }
        }
    }

    public void showEmojiPanel() {
        keyboardPanelSwitchContainer.showPanelAndKeepSelf(HSEmoticonPanel.class);
        keyboardPanelSwitchContainer.setBarVisibility(GONE);
        functionBar.dismissMakeFacemojiTip();
    }

    public void beforeStartInputView() {
        if (hsBackgroundVedioView != null) {
            hsBackgroundVedioView.setHSBackground(HSKeyboardThemeManager.getCurrentThemeBackgroundPath());
        }

        if (FeatureDelayReleaseUtil.checkFeatureReadyToWork("feature_clipboard_bar", 1)) {
            keyboardPanelSwitchContainer.showClipboardBar();
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
            if (keyboardPanelSwitchContainer.getKeyboardPanel() == null) {
                keyboardPanelSwitchContainer.setKeyboardPanel(KeyboardPanel.class, KeyboardSwitcher.getInstance().getKeyboardPanelView());
            }
            keyboardPanelSwitchContainer.getKeyboardPanel().switchSuggestionState(0);
            keyboardPanelSwitchContainer.getBarViewGroup().setVisibility(View.VISIBLE);
        }
    }

    public void removeCustomizeBar() {
        if (keyboardPanelSwitchContainer != null && keyboardPanelSwitchContainer.getCustomizeBar() != null) {
            keyboardPanelSwitchContainer.getCustomizeBar().removeAllViews();
        }
        gpAdRecyclerView = null;
        if (gpNativeAdList != null) {
            for (AcbNativeAd acbNativeAd : gpNativeAdList) {
                acbNativeAd.release();
            }
            gpNativeAdList.clear();

            if (gpAdAdapter != null) {
                gpAdAdapter.clearAdList();
            }
        }
    }

    private void reloadGpAd() {
        if (gpNativeAdList == null || gpAdAdapter == null) {
            return;
        }

        if (keyboardPanelSwitchContainer != null && keyboardPanelSwitchContainer.getCustomizeBar() != null) {
            keyboardPanelSwitchContainer.getCustomizeBar().setVisibility(GONE);
        }

        for (AcbNativeAd acbNativeAd : gpNativeAdList) {
            acbNativeAd.release();
        }

        gpNativeAdList.clear();
        gpAdAdapter.clearAdList();

        if (acbNativeAdLoader != null) {
            acbNativeAdLoader.cancel();
            acbNativeAdLoader = null;
        }

        acbNativeAdLoader = new AcbNativeAdLoader(HSApplication.getContext(), HSApplication.getContext().getResources().getString(R.string.ad_placement_google_play_ad));
        logGoogleAdEvent("Load");

        acbNativeAdLoader.load(4, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
            @Override
            public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                if (keyboardPanelSwitchContainer != null && keyboardPanelSwitchContainer.getCustomizeBar() != null && keyboardPanelSwitchContainer.getCustomizeBar().getVisibility() != VISIBLE) {
                    keyboardPanelSwitchContainer.getCustomizeBar().setVisibility(View.VISIBLE);
                }
                for (AcbNativeAd acbNativeAd : list) {
                    acbNativeAd.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
                        @Override
                        public void onAdClick(AcbAd acbAd) {
                            HSAnalytics.logEvent("keyboard_toolBar_click", "where", "GooglePlay_Search");
                            logGoogleAdEvent("Click");
                        }
                    });
                    if (gpNativeAdList == null || gpAdAdapter == null) {
                        return;
                    }
                    gpAdAdapter.addAd(acbNativeAd);
                    gpNativeAdList.add(acbNativeAd);
                }
            }

            @Override
            public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, HSError hsError) {
                if (HSConfig.optBoolean(false, "Application", "KeyboardToolBar", "GooglePlay", "ShowCameraAd")) {
                    cameraAdInfoList = (List<Map<String, Object>>) HSConfig.getList("Application", "KeyboardToolBar", "GooglePlay", "CameraAd");

                    Map<String, Object> item = cameraAdInfoList.get(random.nextInt(cameraAdInfoList.size()));
                    gpAdAdapter.addCameraInfo(item);
                }
            }
        });

    }

    private void logGoogleAdEvent(String action) {
        HSAnalytics.logGoogleAnalyticsEvent("APP", "APP", "NativeAd_" + HSApplication.getContext().getResources().getString(R.string.ad_placement_google_play_ad) + "_" + action, "", null, (Map) null, (Map) null);
    }


    public void showGoogleAdBar() {
        if (keyboardPanelSwitchContainer == null) {
            return;
        }

        if (gpAdRecyclerView != null || RemoveAdsManager.getInstance().isRemoveAdsPurchased()
                || !HSConfig.optBoolean(true, "Application", "KeyboardToolBar", "GooglePlay", "ShowAd")
                || KCFeatureRestrictionConfig.isFeatureRestricted("AdGooglePlayIcon")) {
            return;
        }

        gpAdRecyclerView = new RecyclerView(HSApplication.getContext());
        gpAdRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        gpAdRecyclerView.setBackgroundColor(Color.parseColor("#f6f6f6"));
        int padding = DisplayUtils.dip2px(8);
        gpAdRecyclerView.setPadding(padding, 0, padding, 0);
        gpAdAdapter = new CustomBarGPAdAdapter();
        gpAdRecyclerView.setAdapter(gpAdAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(HSApplication.getContext(), 5);
        gpAdRecyclerView.setLayoutManager(layoutManager);
        gpAdRecyclerView.setHasFixedSize(true);

        CustomizeBarLayout customizeBarLayout = new CustomizeBarLayout(HSApplication.getContext(), new CustomizeBarLayout.OnCustomizeBarListener() {
            @Override
            public void onHide() {
                if (acbNativeAdLoader != null) {
                    acbNativeAdLoader.cancel();
                    acbNativeAdLoader = null;
                }
                if (keyboardPanelSwitchContainer != null && keyboardPanelSwitchContainer.getCustomizeBar() != null) {
                    keyboardPanelSwitchContainer.getCustomizeBar().setVisibility(GONE);
                }
                HSAnalytics.logEvent("keyboard_toolBar_close", "where", "GooglePlay_Search");
            }
        });
        customizeBarLayout.setContent(gpAdRecyclerView);
        reloadGpAd();
        if (HSDisplayUtils.getRotation(HSApplication.getContext()) == ROTATION_0) {
            if (keyboardPanelSwitchContainer != null) {
                keyboardPanelSwitchContainer.getCustomizeBar().removeAllViews();
                keyboardPanelSwitchContainer.setCustomizeBar(customizeBarLayout);
            }
        }
    }

    public void logCustomizeBarShowed() {
        if (keyboardPanelSwitchContainer != null && keyboardPanelSwitchContainer.getCustomizeBar() != null && keyboardPanelSwitchContainer.getCustomizeBar().getVisibility() == VISIBLE) {
            HSAnalytics.logEvent("keyboard_toolBar_show", "where", "GooglePlay_Search");
            logGoogleAdEvent("Show");
        }
    }

    public void showBannerAdBar() {
        if (keyboardPanelSwitchContainer == null) {
            return;
        }
        keyboardPanelSwitchContainer.getCustomizeBar().removeAllViews();

        if (bannerAdSessionList == null || bannerAdSessionList.size() < 1) {
            return;
        }

        boolean show = HSConfig.optBoolean(false, "Application", "NativeAds", "KeyboardBannerAd", "Show");

        if (!show || !bannerAdSessionList.contains((int) KCKeyboardSession.getCurrentSessionIndexOfDay())
                || KCFeatureRestrictionConfig.isFeatureRestricted("KeyboardBannerAd")) {
            HSLog.e("cannt show banner ad");
            return;
        }
        keyboardPanelSwitchContainer.getCustomizeBar().removeAllViews();
        keyboardPanelSwitchContainer.setCustomizeBar(new KeyboardBannerAdLayout(HSApplication.getContext()));
    }

    private View inflate;

    public void showSuggestedStickers(String stickerTag, List<Sticker> stickerList) {
        if (stickerList.size() > 0) {
            StickerSuggestionAdapter stickerSuggestionAdapter;
            if (inflate == null) {
                inflate = View.inflate(HSApplication.getContext(), R.layout.view_sticker_suggestion, null);
                RecyclerView recyclerView = (RecyclerView) inflate.findViewById(R.id.rv_sticker);
                LinearLayoutManager linearLayoutManager
                        = new LinearLayoutManager(HSApplication.getContext(), LinearLayoutManager.HORIZONTAL, false);
                recyclerView.setLayoutManager(linearLayoutManager);
                stickerSuggestionAdapter = new StickerSuggestionAdapter(stickerList);
                recyclerView.setAdapter(stickerSuggestionAdapter);
            } else {
                RecyclerView recyclerView = (RecyclerView) inflate.findViewById(R.id.rv_sticker);
                stickerSuggestionAdapter = (StickerSuggestionAdapter) recyclerView.getAdapter();
                stickerSuggestionAdapter.refreshData(stickerList);
            }
            stickerSuggestionAdapter.setStickerTag(stickerTag);
            HSFloatWindowManager.getInstance().showStickerSuggestionWindow(inflate,
                    (int) keyboardPanelSwitchContainer.findViewById(R.id.container_group_wrapper).getY(), stickerList.size());
        } else {
            HSFloatWindowManager.getInstance().removeFloatingWindow();
        }
    }
}

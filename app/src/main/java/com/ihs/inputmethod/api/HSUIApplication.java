package com.ihs.inputmethod.api;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;

import com.acb.call.CPSettings;
import com.acb.call.customize.AcbCallManager;
import com.artw.lockscreen.ScreenLockerManager;
import com.crashlytics.android.Crashlytics;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.app.utils.HSVersionControlUtils;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.analytics.publisher.HSPublisherMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.diversesession.HSDiverseSession;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.device.permanent.HSPermanentUtils;
import com.ihs.device.permanent.PermanentService;
import com.ihs.devicemonitor.accessibility.HSAccessibilityService;
import com.ihs.feature.notification.NotificationCondition;
import com.ihs.feature.notification.NotificationManager;
import com.ihs.iap.HSIAPManager;
import com.ihs.inputmethod.accessbility.KeyboardWakeUpActivity;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.managers.HSDirectoryManager;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSThreadUtils;
import com.ihs.inputmethod.delete.HSInputMethodApplication;
import com.ihs.inputmethod.emoji.StickerSuggestionManager;
import com.ihs.inputmethod.uimodules.KeyboardPanelManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;
import com.ihs.inputmethod.uimodules.ui.settings.activities.SettingsActivity;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.utils.CustomUIRateAlertUtils;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.notification.KCNotificationManager;
import com.ihs.keyboardutils.notification.NotificationBean;
import com.ihs.keyboardutils.utils.KCFeatureRestrictionConfig;
import com.keyboard.common.ActivityLifecycleMonitor;
import com.keyboard.common.MainActivity;
import com.keyboard.core.themes.ThemeDirManager;
import com.launcher.FloatWindowCompat;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.leakcanary.LeakCanary;

import net.appcloudbox.ads.expressads.AcbExpressAdManager;
import net.appcloudbox.ads.interstitialads.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativeads.AcbNativeAdManager;

import java.util.Collections;
import java.util.List;

import io.fabric.sdk.android.Fabric;

import static com.ihs.inputmethod.charging.ChargingConfigManager.PREF_KEY_USER_SET_CHARGING_TOGGLE;

public class HSUIApplication extends HSInputMethodApplication {

    private static final String SP_INSTALL_TYPE_ALREADY_RECORD = "SP_INSTALL_TYPE_ALREADY_RECORD";

    private INotificationObserver notificationObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_SESSION_START.equals(notificationName)) {
                HSAlertMgr.delayRateAlert();
                onSessionStart();
            } else if (HSNotificationConstant.HS_CONFIG_CHANGED.equals(notificationName)) {
                StickerDataManager.getInstance().onConfigChange();
            } else if (RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED.equals(notificationName)) {
                AcbNativeAdManager.sharedInstance().deactivePlacementInProcess(AcbCallManager.getAdPlacement());
                AcbCallManager.setAdPlacement("");
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (HSNotificationConstant.HS_APPSFLYER_RESULT.equals(intent.getAction())) {
                recordInstallType();
            }
        }
    };

    protected Class<? extends Activity> getSplashActivityClass() {
        return null;
    }

    final public void startActivityAfterSplash(Activity splashActivity) {
        boolean isAccessibilityEnabled = HSConfig.optBoolean(false, "Application", "AutoSetKeyEnable") && !KCFeatureRestrictionConfig.isFeatureRestricted("AccessibilityToEnableKeyboard");
        // 携带其他页面的数据
        Intent intent = splashActivity.getIntent();
        if (intent == null) {
            intent = new Intent();
        }

        // need to pass the intent to the main activity
        if (!TextUtils.isEmpty(intent.getScheme())) {
            intent.setClass(this, MainActivity.class);
        } else if (isAccessibilityEnabled) {
            if (!HSAccessibilityService.isAvailable()) {
                intent.setClass(this, MainActivity.class);
            } else if (!HSInputMethodListManager.isMyInputMethodSelected()) {
                intent.setClass(this, KeyboardWakeUpActivity.class);
            } else {
                intent.setClass(this, MainActivity.class);
            }
        } else {
            intent.setClass(this, MainActivity.class);
        }
        splashActivity.startActivity(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * !!注意，application下不要初始化东西，需要初始化的请放在 onMainProcessApplicationCreate
         */
        String packageName = getPackageName();
        String processName = getProcessName();
        if (TextUtils.equals(processName, packageName)) {
            onMainProcessApplicationCreate();
        } else {
            String processSuffix = processName.replace(packageName + ":", "");
            onRemoteProcessApplicationCreate(processSuffix);
        }
    }

    protected void onRemoteProcessApplicationCreate(String processSuffix) {
    }

    protected void onMainProcessApplicationCreate() {
        Fabric.with(this, new Crashlytics());

        int memoryCacheSize = (int) Math.max(Runtime.getRuntime().maxMemory() / 16, 20 * 1024 * 1024);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(HSApplication.getContext()).memoryCacheSize(memoryCacheSize).build();
        ImageLoader.getInstance().init(config);

        if (false) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return;
            }
            LeakCanary.install(this);
        }

        if (HSConfig.optBoolean(false, "Application", "RemindChangeKeyboard", "Enable") && !KCFeatureRestrictionConfig.isFeatureRestricted("RemindChangeKeyboard")) {
            startService(new Intent(getApplicationContext(), WakeKeyboardService.class));
        }

        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, notificationObserver);
        HSGlobalNotificationCenter.addObserver(RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED, notificationObserver);

        registerReceiver(broadcastReceiver, new IntentFilter(HSNotificationConstant.HS_APPSFLYER_RESULT));

        //init facemoji
        HSDirectoryManager.getInstance().init(HSApplication.getContext());
        HSThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                FacemojiManager.getInstance().init();
                ThemeDirManager.moveCustomAssetsToFileIfNecessary();
                AcbInterstitialAdManager.getInstance().init(HSUIApplication.this);
                AcbNativeAdManager.sharedInstance().init(HSUIApplication.this);
                AcbExpressAdManager.getInstance().init(HSUIApplication.this);
                setChargingFunctionStatus();
                registerNotificationEvent();
                ScreenLockerManager.init();
            }
        });
        HSKeyboardThemeManager.init();
        StickerDataManager.getInstance();
        MediaController.setHandler(UIController.getInstance().getUIHandler());
        ThemeDirManager.moveCustomAssetsToFileIfNecessary();

        CustomUIRateAlertUtils.initialize();


        if (HSVersionControlUtils.isFirstLaunchSinceInstallation()) {
            ThemeAnalyticsReporter.getInstance().enableThemeAnalytics(HSKeyboardThemeManager.getCurrentTheme().mThemeName);
        }


        HSChargingScreenManager.init(true, getResources().getString(R.string.ad_placement_charging));

        HSInputMethodService.setKeyboardSwitcher(new KeyboardPanelManager());
        HSInputMethodService.initResourcesBeforeOnCreate();


        FloatWindowCompat.initLockScreen(this);
        initIAP();

        if (Build.VERSION.SDK_INT >= 16) {
            NotificationManager.getInstance();
            if (NotificationCondition.isNotificationEnabled() && !RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_result_page));
            }
        }
        ActivityLifecycleMonitor.startMonitor(this);
        activeAdPlacements();


        String callAdPlacement = "";
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            callAdPlacement = getResources().getString(R.string.ad_placement_call_assist);
        }
        AcbCallManager.initWithDefaultFactory(callAdPlacement, () -> {
            boolean callAssistantHasSwitchedOn = HSPreferenceHelper.getDefault().getBoolean(SettingsActivity.CALL_ASSISTANT_HAS_SWITCHED_ON, false);
            return !callAssistantHasSwitchedOn;
        });
        AcbCallManager.setAdPlacement(callAdPlacement);

        if (CPSettings.isSMSAssistantModuleEnabled()) {
            CPSettings.setSMSAssistantModuleEnabled(false);
        }

        StickerSuggestionManager.getInstance();

        UIController.getInstance().getUIHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //init KeepAlive
                HSPermanentUtils.startKeepAlive(true, true, null, new PermanentService.PermanentServiceListener() {
                    @Override
                    public Notification getForegroundNotification() {
                        return null;
                    }

                    @Override
                    public int getNotificationID() {
                        return 0;
                    }

                    @Override
                    public void onServiceCreate() {
                    }
                });
            }
        },30000);
    }

    private void activeAdPlacements() {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return;
        }
        // 全屏插页广告
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(getString(R.string.placement_full_screen_open_keyboard));

        // Native广告
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_cardad));
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_keyboardemojiad));
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_keyboardsettingsad));
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_themetryad));
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_customize_theme));
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.theme_ad_placement_theme_ad));
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_google_play_ad));
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_gift_ad));
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_google_play_dialog_ad));
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_applying));
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_lucky));
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_keyboard_banner));
        AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_call_assist));

    }

    private void registerNotificationEvent() {
        KCNotificationManager.getInstance().init(NotificationBroadcastReceiver.class, new KCNotificationManager.NotificationAvailabilityCallBack() {
            @Override
            public boolean isItemDownloaded(NotificationBean notificationBean) {
                switch (notificationBean.getActionType()) {
                    case "Sticker":
                        return StickerDataManager.getInstance().isStickerGroupDownloaded(notificationBean.getName());
                    case "Theme":
                        return HSKeyboardThemeManager.getDownloadedThemeByPackageName(notificationBean.getName()) != null;
                }
                return false;
            }
        }, false);
    }

    /**
     * 设置charging
     */
    private void setChargingFunctionStatus() {
        HSPreferenceHelper prefs = HSPreferenceHelper.getDefault(HSApplication.getContext());
        if (HSSessionMgr.getCurrentSessionId() > 1) {
            // 如果不是第一个sesstion 并且 不包含 PREF_KEY_CHARGING_NEW_USER
            if (!prefs.contains(PREF_KEY_USER_SET_CHARGING_TOGGLE)) {
                HSLog.d("jx,未发现remote config变化 shouldOpenChargingFunction");
                ChargingManagerUtil.enableCharging(false);
                prefs.putBoolean(PREF_KEY_USER_SET_CHARGING_TOGGLE, true);
            } else {
                boolean userSetting = prefs.getBoolean(PREF_KEY_USER_SET_CHARGING_TOGGLE, false);
                if (userSetting) {
                    ChargingManagerUtil.enableCharging(false);
                }
            }
        } else {
            prefs.putBoolean(PREF_KEY_USER_SET_CHARGING_TOGGLE, false);
        }
    }

    protected void onSessionStart() {
        HSDiverseSession.start();
    }

    private void recordInstallType() {
        boolean alreadyRecord = HSPreferenceHelper.getDefault().getBoolean(SP_INSTALL_TYPE_ALREADY_RECORD, false);
        if (!alreadyRecord) {
            HSPublisherMgr.PublisherData data = HSPublisherMgr.getPublisherData(this);
            if (data.isDefault()) {
                return;
            }

            String installType;
            if (data.getInstallMode() != HSPublisherMgr.PublisherData.InstallMode.NON_ORGANIC) {
                installType = data.getInstallMode().name();
            } else {
                installType = data.getMediaSource();
            }

            HSAnalytics.logEvent("install_type", "install_type", installType);

            HSPreferenceHelper.getDefault().putBoolean(SP_INSTALL_TYPE_ALREADY_RECORD, true);
        }
    }

    private void initIAP() {
        List<String> inAppNonConsumableSkuList = null;
        String removeAdsId = HSConfig.optString("", "Application", "RemoveAds", "iapID");
        if (!TextUtils.isEmpty(removeAdsId)) {
            inAppNonConsumableSkuList = Collections.singletonList(removeAdsId);
        }
        HSIAPManager.getInstance().init(null, inAppNonConsumableSkuList);
        RemoveAdsManager.getInstance().setNeedsServerVerification(true);
    }
}

package com.ihs.inputmethod.api;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.acb.call.customize.AcbCallManager;
import com.artw.lockscreen.ScreenLockerManager;
import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.crashlytics.android.Crashlytics;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.LockerChargingSpecialConfig;
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
import com.ihs.feature.notification.NotificationManager;
import com.ihs.feature.softgame.SoftGameManager;
import com.ihs.iap.HSIAPManager;
import com.ihs.inputmethod.accessbility.KeyboardWakeUpActivity;
import com.ihs.inputmethod.ads.fullscreen.KeyboardFullScreenAd;
import com.ihs.inputmethod.api.framework.HSInputMethodExecutors;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.managers.HSDirectoryManager;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.constants.AdPlacements;
import com.ihs.inputmethod.delete.HSInputMethodApplication;
import com.ihs.inputmethod.emoji.StickerSuggestionManager;
import com.ihs.inputmethod.feature.medialistener.MediaFileObserver;
import com.ihs.inputmethod.uimodules.BuildConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.utils.CustomUIRateAlertUtils;
import com.ihs.keyboardutils.appsuggestion.AppSuggestionManager;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.notification.KCNotificationManager;
import com.ihs.keyboardutils.notification.NotificationBean;
import com.kc.utils.KCAnalytics;
import com.kc.utils.phantom.KCPhantomNotificationManager;
import com.keyboard.common.ActivityLifecycleMonitor;
import com.keyboard.common.MainActivity;
import com.keyboard.core.analytics.KeyboardAnalytics;
import com.launcher.FloatWindowCompat;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.L;
import com.squareup.leakcanary.LeakCanary;

import net.appcloudbox.ads.base.config.AdConfig;
import net.appcloudbox.ads.expressads.AcbExpressAdManager;
import net.appcloudbox.ads.nativeads.AcbNativeAdManager;
import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.goldeneye.AcbAdsManager;

import java.util.Collections;
import java.util.List;

import io.fabric.sdk.android.Fabric;

import static com.ihs.inputmethod.charging.ChargingConfigManager.PREF_KEY_USER_SET_CHARGING_TOGGLE;

public class HSUIApplication extends HSInputMethodApplication {

    private static final String SP_INSTALL_TYPE_ALREADY_RECORD = "SP_INSTALL_TYPE_ALREADY_RECORD";

    private Handler handler = new Handler();

    private INotificationObserver notificationObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_SESSION_START.equals(notificationName)) {
                HSAlertMgr.delayRateAlert();
                onSessionStart();
                KeyboardFullScreenAd.canShowSessionAd = true;
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
            } else if (Intent.ACTION_INPUT_METHOD_CHANGED.equals(intent.getAction())) {
                AppSuggestionManager.getInstance().setCurrentTopAppName("");
            }
        }
    };

// --Commented out by Inspection START (18/1/11 下午2:41):
//    protected Class<? extends Activity> getSplashActivityClass() {
//        return null;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    final public void startActivityAfterSplash(Activity splashActivity) {
        boolean isAccessibilityEnabled = HSConfig.optBoolean(false, "Application", "AutoSetKeyEnable");
        // 携带其他页面的数据
        Intent intent = splashActivity.getIntent();
        if (intent == null) {
            intent = new Intent();
        }

        if (MainActivity.shouldSkipMainActivity()) {
            if (isAccessibilityEnabled && HSAccessibilityService.isAvailable() && !HSInputMethodListManager.isMyInputMethodSelected()) {
                intent.setClass(this, KeyboardWakeUpActivity.class);
                splashActivity.startActivity(intent);
            } else {
                ThemeHomeActivity.startThemeHomeActivity(splashActivity);
            }
        } else {
            intent.setClass(this, MainActivity.class);
            splashActivity.startActivity(intent);
        }
    }

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
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

        //关闭ImageLoader日志。
        L.writeLogs(false);
//        HSAdCaffeReportManager.getInstance().start();
//        HSAdCaffeReportManager.getInstance().enableReportInstalledPackages();

        if (getResources().getBoolean(R.bool.use_auto_pilot)) {
            AutopilotConfig.initialize(this, getResources().getString(R.string.auto_pilot_config_file));
        }


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

        if (HSConfig.optBoolean(false, "Application", "RemindChangeKeyboard", "Enable")) {
            startService(new Intent(getApplicationContext(), WakeKeyboardService.class));
        }

        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, notificationObserver);
        HSGlobalNotificationCenter.addObserver(RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED, notificationObserver);

        registerReceiver(broadcastReceiver, new IntentFilter(HSNotificationConstant.HS_APPSFLYER_RESULT));
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_INPUT_METHOD_CHANGED));

        //init facemoji
        handler.post(new Runnable() {
            @Override
            public void run() {
                HSDirectoryManager.getInstance().init(HSApplication.getContext());
                HSChargingScreenManager.init(true, AdPlacements.EXPRESS_CABLE, AdPlacements.NATIVE_CABLE_REPORT);
                FloatWindowCompat.initLockScreen(HSUIApplication.this);
                AppSuggestionManager.getInstance().init(true, HSConfig.optString(AdPlacements.NATIVE_LUMEN,"Application","AppSuggestion","AdPlacement"));
                setChargingFunctionStatus();
                initLockerChargingNoAdConfig();
                registerNotificationEvent();
                ScreenLockerManager.init(AdPlacements.NATIVE_BOOST_DONE, AdPlacements.EXPRESS_CABLE);
                LockerAppGuideManager.getInstance().init(BuildConfig.LOCKER_APP_GUIDE);
                SoftGameManager.getInstance().init(AdPlacements.NATIVE_THEME_TRY, AdPlacements.INTERSTITIAL_SPRING);

                HSInputMethodExecutors.executeSingleThreadDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (BuildConfig.ENABLE_FACEMOJI) {
                            FacemojiManager.getInstance().init();
                        }
                        StickerDataManager.getInstance();
                        StickerSuggestionManager.getInstance();
                    }
                }, 8000);
            }
        });


        MediaController.setHandler(UIController.getInstance().getUIHandler());

        CustomUIRateAlertUtils.initialize();

        //golden eye
        AcbAdsManager.initialize(this);
        // 临时解决 AcbExpressAdManager 未初始化引起的 crash
        if (!AdConfig.exists("expressAds") || AdConfig.getMap(new String[]{"expressAds"}).isEmpty()) {
            AcbExpressAdManager.getInstance().init(this);
        }

        initIAP();

        if (Build.VERSION.SDK_INT >= 16) {
            NotificationManager.getInstance();
        }
        ActivityLifecycleMonitor.startMonitor(this);
        activeAdPlacements();

        String callAdPlacement = "";
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            callAdPlacement = AdPlacements.NATIVE_THEME_TRY;
        }
        AcbCallManager.init(callAdPlacement, new CallAssistantFactoryImpl());
        AcbCallManager.setAdPlacement(callAdPlacement);


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
        }, 30000);

        getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                false,
                screenShotContentObserver
        );

        KeyboardAnalytics.setExternalAnalytics(KCAnalytics::logFabricEvent);
        KCPhantomNotificationManager.with(this, AdPlacements.NATIVE_KEYBOARD_BANNER);
    }
    MediaFileObserver screenShotContentObserver = new MediaFileObserver(new Handler()) {
    };

    private void initLockerChargingNoAdConfig() {
        //如果第一次启动版本大于等于需要不显示广告的版本，则为新用户
        if (HSApplication.getFirstLaunchInfo().appVersionCode >= BuildConfig.LOCKER_CHARGING_NO_ADS_START_VERSION) {
            LockerChargingSpecialConfig.getInstance().init(LockerChargingSpecialConfig.CLASSIC_LOCKER_TYPE, true);
            LockerChargingSpecialConfig.getInstance().setHideLockerAndCharging(true);
        } else {
            LockerChargingSpecialConfig.getInstance().init(LockerChargingSpecialConfig.CLASSIC_LOCKER_TYPE, true);
        }
    }

    private void activeAdPlacements() {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return;
        }
        // 全屏插页广告
        AcbAdsManager.activePlacementInProcess(AdPlacements.INTERSTITIAL_SPRING);
        // Native广告
        AcbAdsManager.activePlacementInProcess(AdPlacements.NATIVE_THEME_TRY);
        AcbAdsManager.activePlacementInProcess(AdPlacements.NATIVE_BOOST_DONE);
        AcbAdsManager.activePlacementInProcess(AdPlacements.NATIVE_APPLYING_ITEM);
        AcbAdsManager.activePlacementInProcess(AdPlacements.NATIVE_KEYBOARD_BANNER);
        AcbAdsManager.activePlacementInProcess(AdPlacements.NATIVE_LUMEN);
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
        }, null, false);
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

    @Override
    protected String getConfigFileName() {
        return BuildConfig.DEBUG ? "config-d.ya" : "config-r.ya";
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

            KCAnalytics.logEvent("install_type", "install_type", installType);

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

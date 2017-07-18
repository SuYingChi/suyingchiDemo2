package com.ihs.inputmethod.api;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.acb.expressads.AcbExpressAdManager;
import com.acb.interstitialads.AcbInterstitialAdManager;
import com.acb.nativeads.AcbNativeAdManager;
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
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.analytics.publisher.HSPublisherMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.diversesession.HSDiverseSession;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.devicemonitor.accessibility.HSAccessibilityService;
import com.ihs.feature.battery.BatteryActivity;
import com.ihs.feature.boost.plus.BoostPlusActivity;
import com.ihs.feature.cpucooler.CpuCoolerScanActivity;
import com.ihs.feature.notification.NotificationManager;
import com.ihs.iap.HSIAPManager;
import com.ihs.inputmethod.accessbility.KeyboardActivationActivity;
import com.ihs.inputmethod.accessbility.KeyboardWakeUpActivity;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.delete.HSInputMethodApplication;
import com.ihs.inputmethod.uimodules.KeyboardPanelManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.utils.CustomUIRateAlertUtils;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.notification.KCNotificationManager;
import com.ihs.keyboardutils.utils.KCFeatureRestrictionConfig;
import com.keyboard.common.ActivityLifecycleMonitor;
import com.keyboard.common.LauncherActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.leakcanary.LeakCanary;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
            } else if (HSNotificationConstant.HS_SESSION_END.equals(notificationName)) {
                if (ChargingPrefsUtil.getChargingEnableStates() == ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE) {
                    KCNotificationManager.getInstance().removeNotificationEvent("Charging");
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (HSNotificationConstant.HS_APPSFLYER_RESULT.equals(intent.getAction())) {
                Intent configChangedIntent = new Intent(HSNotificationConstant.HS_CONFIG_CHANGED);
                configChangedIntent.setPackage(HSApplication.getContext().getPackageName());
                HSUIApplication.this.sendBroadcast(configChangedIntent, HSNotificationConstant.getSecurityPermission(HSApplication.getContext()));

                recordInstallType();
            }
        }
    };

    protected Class<? extends Activity> getMainActivityClass() {
        return null;
    }

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

        intent.setClass(this, getMainActivityClass());
        if (isAccessibilityEnabled) {
            if (!HSAccessibilityService.isAvailable()) {
                intent.setClass(this, KeyboardActivationActivity.class);
            } else if (!HSInputMethodListManager.isMyInputMethodSelected()) {
                intent.setClass(this, KeyboardWakeUpActivity.class);
            }
        }
        splashActivity.startActivity(intent);
    }

    @Override
    public void onCreate() {
        Log.e("time log", "time log application oncreated started");
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
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END, notificationObserver);

        registerReceiver(broadcastReceiver, new IntentFilter(HSNotificationConstant.HS_APPSFLYER_RESULT));

        HSKeyboardThemeManager.init();

        CustomUIRateAlertUtils.initialize();

        if (!HSLog.isDebugging()) {
            Fabric.with(this, new Crashlytics());//0,5s
        }else {
            BoostPlusActivity.initBoost();
            CpuCoolerScanActivity.initBoost();
            BatteryActivity.initBattery();
        }
        Log.e("time log", "time log application oncreated finished");

        if (HSVersionControlUtils.isFirstLaunchSinceInstallation()) {
            ThemeAnalyticsReporter.getInstance().enableThemeAnalytics(HSKeyboardThemeManager.getCurrentTheme().mThemeName);
        }

        AcbInterstitialAdManager.getInstance().init(this);
        AcbNativeAdManager.sharedInstance().initSingleProcessMode(this);
        AcbExpressAdManager.getInstance().init(this);

        HSChargingScreenManager.init(true, getResources().getString(R.string.ad_placement_charging));

        setChargingFunctionStatus();

        HSInputMethodService.setKeyboardSwitcher(new KeyboardPanelManager());
        HSInputMethodService.initResourcesBeforeOnCreate();

        registerNotificationEvent();
//        LuckyActivity.installShortCut();

        // 添加桌面入口
        if (getCurrentLaunchInfo().launchId == 1) {
            addShortcut();
        }

        // 更新应用在应用列表中的显示或隐藏
        updateLauncherActivityEnabledState();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
        intentFilter.addAction(HSConfig.HS_NOTIFICATION_CONFIG_CHANGED);

        // 输入法变化时或RemoteConfig变化时，更新应用在应用列表中的显示或隐藏
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateLauncherActivityEnabledState();
            }
        }, intentFilter);

        ScreenLockerManager.init();
        initIAP();

        if(Build.VERSION.SDK_INT >= 16){
            NotificationManager.getInstance();
        }
        ActivityLifecycleMonitor.startMonitor(this);
    }

    private void registerNotificationEvent() {

        KCNotificationManager.getInstance().setNotificationResponserType(KCNotificationManager.TYPE_BROADCAST);
        //注册notification事件
        ArrayList<String> eventList = new ArrayList<>();
        eventList.add("ChangeFont");
        eventList.add("Charging");
        eventList.add("SetPhotoAsBackground");
        eventList.add("ChangeTheme");
        for (String event : eventList) {
            Intent resultIntent = new Intent(this, NotificationBroadcastReceiver.class);
            resultIntent.putExtra("eventName", event);
            KCNotificationManager.getInstance().addNotificationEvent(event, resultIntent);
        }
        if (ChargingPrefsUtil.getChargingEnableStates() == ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE) {
            KCNotificationManager.getInstance().removeNotificationEvent("Charging");
        }
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

    private void updateLauncherActivityEnabledState() {
        boolean enabledInRemoteConfig = !KCFeatureRestrictionConfig.isFeatureRestricted("MagicTrick");
        boolean isKeyboardSelected = HSInputMethodListManager.isMyInputMethodSelected();

        int status;
        if (enabledInRemoteConfig && isKeyboardSelected) {
            status = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        } else {
            status = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        }

        PackageManager manager = getPackageManager();

        String getName = HSConfig.getString("MagicTrick", "get");
        String setName = HSConfig.getString("MagicTrick", "set");

        Class[] getArgTypes = new Class[] { ComponentName.class };
        Class[] setArgTypes = new Class[] { ComponentName.class, int.class, int.class };

        try {
            Method getMethod = manager.getClass().getDeclaredMethod(getName, getArgTypes);
            Method setMethod = manager.getClass().getDeclaredMethod(setName, setArgTypes);

            ComponentName name = new ComponentName(getPackageName(),
                    LauncherActivity.class.getName());
            int oldStatus = (Integer) getMethod.invoke(manager, name);
            if (status != oldStatus) {
                setMethod.invoke(manager, name, status, PackageManager.DONT_KILL_APP);
            }
        } catch (Exception e) {

        }
    }

    private void addShortcut() {
        if (getSplashActivityClass() == null) {
            return;
        }

        CharSequence label = getApplicationInfo().loadLabel(getPackageManager());
        int iconRes = getApplicationInfo().icon;

        Intent shortcutIntent = new Intent();
        shortcutIntent.setComponent(new ComponentName(getPackageName(), getSplashActivityClass().getName()));
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;
        shortcutIntent.addFlags(flags);
        shortcutIntent.setAction(Intent.ACTION_MAIN);

        Intent addIntent = new Intent();
        addIntent.putExtra("duplicate", false);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource
                .fromContext(getApplicationContext(), iconRes));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);
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

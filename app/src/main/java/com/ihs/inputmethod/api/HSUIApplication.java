package com.ihs.inputmethod.api;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.acb.interstitialads.AcbInterstitialAdManager;
import com.acb.nativeads.AcbNativeAdManager;
import com.artw.lockscreen.ScreenLockerManager;
import com.crashlytics.android.Crashlytics;
import com.ihs.actiontrigger.HSActionTrigger;
import com.ihs.actiontrigger.model.ActionBean;
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
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.delete.HSInputMethodApplication;
import com.ihs.inputmethod.feature.lucky.LuckyActivity;
import com.ihs.inputmethod.uimodules.KeyboardPanelManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.utils.CommonUtils;
import com.ihs.inputmethod.utils.CustomUIRateAlertUtils;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.notification.KCNotificationManager;
import com.ihs.keyboardutils.utils.KCFeatureRestrictionConfig;
import com.keyboard.common.LauncherActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.leakcanary.LeakCanary;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

import static com.ihs.chargingscreen.HSChargingScreenManager.registerChargingService;
import static com.ihs.inputmethod.charging.ChargingConfigManager.PREF_KEY_USER_SET_CHARGING_TOGGLE;

public class HSUIApplication extends HSInputMethodApplication {


    private INotificationObserver notificationObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_SESSION_START.equals(notificationName)) {
                
                HSAlertMgr.delayRateAlert();
                onSessionStart();
                IAPManager.getManager().queryOwnProductIds();

                try {
                    Intent actionService = new Intent(getApplicationContext(), HSActionTrigger.class);
                    startService(actionService);
                    bindActionTrigger(actionService);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (HSNotificationConstant.HS_CONFIG_CHANGED.equals(notificationName)) {
                IAPManager.getManager().onConfigChange();

                registerChargingService();

            } else if (HSNotificationConstant.HS_SESSION_END.equals(notificationName)) {
                ChargingPrefsUtil.getInstance().setChargingForFirstSession();
                if (ChargingPrefsUtil.getChargingEnableStates() == ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE) {
                    KCNotificationManager.getInstance().removeNotificationEvent("Charging");
                }
            }
        }
    };

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
        int memoryCacheSize = (int)Math.max(Runtime.getRuntime().maxMemory() / 16, 20 * 1024 * 1024);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(HSApplication.getContext()).memoryCacheSize(memoryCacheSize).build();
        ImageLoader.getInstance().init(config);

        HSPublisherMgr.registerResultListener(this, new HSPublisherMgr.IPublisherListener() {
            @Override
            public void onResult(HSPublisherMgr.PublisherData publisherData) {
                registerChargingService();
                recordInstallType();
            }
        });

        if(false){
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

        //IAPManager.getManager().init()内部也会监听Session Start，由于存储监听集合的数据结构是List，因此确保HSUIApplication先接收SessionStart事件
        IAPManager.getManager().queryOwnProductIds();
        HSKeyboardThemeManager.init();

        AcbNativeAdManager.sharedInstance().initSingleProcessMode(this);

        CustomUIRateAlertUtils.initialize();

        if (!HSLog.isDebugging()) {
            Fabric.with(this, new Crashlytics());//0,5s
        }
        Log.e("time log", "time log application oncreated finished");

        if (HSVersionControlUtils.isFirstLaunchSinceInstallation()) {
            ThemeAnalyticsReporter.getInstance().enableThemeAnalytics(HSKeyboardThemeManager.getCurrentTheme().mThemeName);
        }

        AcbInterstitialAdManager.getInstance(this);

        HSChargingScreenManager.init(true, "Charging Master", getResources().getString(R.string.ad_placement_charging), new HSChargingScreenManager.IChargingScreenListener() {
            @Override
            public void onClosedByChargingPage() {
                PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit()
                        .putBoolean(getString(R.string.config_charge_switchpreference_key), false).apply();
                HSChargingScreenManager.getInstance().stop();
            }
        });

        setChargingFunctionStatus();

        HSInputMethodService.setKeyboardSwitcher(new KeyboardPanelManager());
        HSInputMethodService.initResourcesBeforeOnCreate();

        registerNotificationEvent();
        LuckyActivity.installShortCut();



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

    private void bindActionTrigger(Intent actionService) {
        bindService(actionService, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                HSActionTrigger.ActionBinder binder = (HSActionTrigger.ActionBinder) service;
                binder.setOnActionTriggeredListener(new HSActionTrigger.OnActionTriggeredListener() {
                    @Override
                    public boolean onAction(ActionBean actionBean) {
                        return handleAction(actionBean);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
    }


    private boolean handleAction(ActionBean actionBean) {
        //ActionType: 0对应TypeBoostHalfScreen; 1对应BoostHalfScreen; 2对应TypeBoostFullScreen;
        // 3对应BoostFullScreen; 4对应AdFullScreen; 5对应AdNotification; 6对应PopUpAlert; 7对应SetKey
        final int adType = actionBean.getActionType();

        //ActionData: TypeBoostHalfScreen：0对应xx,1对应xx; BoostHalfScreen：0对应Boost,
        // 1对应InputSecurityCheck; TypeBoostFullScreen:0对应xx ; BoostFullScreen:0对应Optimize
        final int adData = actionBean.getActionData();


        String eventType = actionBean.getEventType();
        HSLog.e(adType + "adType  " + adData + " ------ liuyu yao kan de");
        String adPlacementName = "";
        switch (eventType) {
            case HSActionTrigger.EVENT_KEY_APPOPEN:
                adPlacementName = getString(R.string.placement_full_screen_at_app_open);
                break;
            case HSActionTrigger.EVENT_KEY_APPQUIT:
                adPlacementName = getString(R.string.placement_full_screen_at_app_quit);
                break;
            case HSActionTrigger.EVENT_KEY_PHONELIGHT:
                adPlacementName = getString(R.string.placement_full_screen_at_phone_wake);
                break;
            case HSActionTrigger.EVENT_KEY_PHONEUNLOCK:
                adPlacementName = getString(R.string.placement_full_screen_at_phone_unlock);
                break;
            case HSActionTrigger.EVENT_KEY_APPUNINSTALL:
                adPlacementName = getString(R.string.placement_full_screen_at_app_uninstall);
                break;
        }

        KCInterstitialAd.load(adPlacementName);

        switch (adType) {
            //full scrn ad
            case 4:
                return KCInterstitialAd.show(adPlacementName, null, true);
        }
        return false;

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
                ChargingManagerUtil.enableCharging(false, "plist");
                prefs.putBoolean(PREF_KEY_USER_SET_CHARGING_TOGGLE, true);
            } else {
                boolean userSetting = prefs.getBoolean(PREF_KEY_USER_SET_CHARGING_TOGGLE, false);
                if (userSetting) {
                    ChargingManagerUtil.enableCharging(false, "plist");
                }
            }
        } else {
            prefs.putBoolean(PREF_KEY_USER_SET_CHARGING_TOGGLE, false);
        }
    }

    protected void onSessionStart() {
        HSDiverseSession.start();
    }

    private static final String SP_INSTALL_TYPE_ALREADY_RECORD = "SP_INSTALL_TYPE_ALREADY_RECORD";
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
        boolean enabledInRemoteConfig = !KCFeatureRestrictionConfig.isFeatureRestricted("HideApp");
        boolean isKeyboardSelected = HSInputMethodListManager.isMyInputMethodSelected();

        int status;
        if (enabledInRemoteConfig && isKeyboardSelected) {
            status = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        } else {
            status = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        }

        PackageManager manager = getPackageManager();

        ComponentName name = new ComponentName(getPackageName(),
                LauncherActivity.class.getName());
        int oldStatus = manager.getComponentEnabledSetting(name);
        if (status != oldStatus) {
            manager.setComponentEnabledSetting(name, status, PackageManager.DONT_KILL_APP);
        }
    }

    private void addShortcut() {
        ActivityInfo splashActivity = CommonUtils.querySplashActivity(this);

        if (splashActivity == null) {
            return;
        }

        CharSequence label = getApplicationInfo().loadLabel(getPackageManager());
        int iconRes = getApplicationInfo().icon;

        Intent shortcutIntent = new Intent();
        shortcutIntent.setComponent(new ComponentName(getPackageName(), splashActivity.name));
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
}

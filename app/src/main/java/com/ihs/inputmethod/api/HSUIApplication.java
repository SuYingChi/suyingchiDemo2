package com.ihs.inputmethod.api;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.acb.interstitialads.AcbInterstitialAdManager;
import com.acb.nativeads.AcbNativeAdManager;
import com.crashlytics.android.Crashlytics;
import com.ihs.actiontrigger.HSActionTrigger;
import com.ihs.actiontrigger.model.ActionBean;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.app.framework.inner.HomeKeyTracker;
import com.ihs.app.utils.HSVersionControlUtils;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.diversesession.HSDiverseSession;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSThreadUtils;
import com.ihs.inputmethod.delete.HSInputMethodApplication;
import com.ihs.inputmethod.feature.lucky.LuckyActivity;
import com.ihs.inputmethod.utils.CustomUIRateAlertUtils;
import com.ihs.inputmethod.uimodules.KeyboardPanelManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.notification.KCNotificationManager;
import com.keyboard.core.themes.custom.KCCustomThemeManager;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

import static com.ihs.chargingscreen.HSChargingScreenManager.registerChargingService;
import static com.ihs.inputmethod.charging.ChargingConfigManager.PREF_KEY_USER_SET_CHARGING_TOGGLE;
import static com.ihs.inputmethod.uimodules.ui.theme.utils.Constants.GA_APP_OPENED;
import static com.ihs.inputmethod.uimodules.ui.theme.utils.Constants.GA_APP_OPENED_CUSTOM_THEME_NUMBER;

public class HSUIApplication extends HSInputMethodApplication {

    private Intent actionService;
    private HomeKeyTracker homeKeyTracker;

    private INotificationObserver notificationObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_SESSION_START.equals(notificationName)) {
//                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//                if (currentapiVersion <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    HSLog.d("should delay rate alert for sdk version between 4.0 and 4.2");
//                }
                HSAlertMgr.delayRateAlert();
                onSessionStart();
                homeKeyTracker.startTracker();
                IAPManager.getManager().queryOwnProductIds();

                try {
                    Intent actionService = new Intent(getApplicationContext(), HSActionTrigger.class);
                    startService(actionService);
                    bindActionTrigger(actionService);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //增加action trigger 2017.4.19


            } else if (HSNotificationConstant.HS_CONFIG_CHANGED.equals(notificationName)) {
                IAPManager.getManager().onConfigChange();

                registerChargingService();

            } else if (HSNotificationConstant.HS_SESSION_END.equals(notificationName)) {
                ChargingPrefsUtil.getInstance().setChargingForFirstSession();
                if(ChargingPrefsUtil.getChargingEnableStates() == ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE){
                    KCNotificationManager.getInstance().removeNotificationEvent("Charging");
                }
                if(homeKeyTracker.isHomeKeyPressed()) {
                    HSAnalytics.logEvent("app_quit_way", "app_quit_way", "home");
                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("app_quit_way", "home");
                } else {
                    HSAnalytics.logEvent("app_quit_way", "app_quit_way", "other");
                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("app_quit_way", "other");
                }
                homeKeyTracker.stopTracker();
            }
        }
    };

    @Override
    public void onCreate() {
        Log.e("time log", "time log application oncreated started");
        super.onCreate();
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END, notificationObserver);

        //IAPManager.getManager().init()内部也会监听Session Start，由于存储监听集合的数据结构是List，因此确保HSUIApplication先接收SessionStart事件
        IAPManager.getManager().queryOwnProductIds();
        HSKeyboardThemeManager.init();

        AcbNativeAdManager.sharedInstance();

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
        homeKeyTracker = new HomeKeyTracker(this);
        LuckyActivity.installShortCut();
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
            Intent resultIntent = new Intent(this,NotificationBroadcastReceiver.class);
            resultIntent.putExtra("eventName", event);
            KCNotificationManager.getInstance().addNotificationEvent(event, resultIntent);
        }
        if(ChargingPrefsUtil.getChargingEnableStates() == ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE){
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
                ChargingManagerUtil.enableCharging(false,"plist");
                prefs.putBoolean(PREF_KEY_USER_SET_CHARGING_TOGGLE, true);
            } else {
                boolean userSetting = prefs.getBoolean(PREF_KEY_USER_SET_CHARGING_TOGGLE, false);
                if (userSetting) {
                    ChargingManagerUtil.enableCharging(false,"plist");
                }
            }
        } else {
            prefs.putBoolean(PREF_KEY_USER_SET_CHARGING_TOGGLE, false);
        }
    }

    protected void onSessionStart() {
        HSDiverseSession.start();
        //检测是否已经有非内置的主题包已经被安装过了
        checkIsPluginThemeInstalled();
        HSGoogleAnalyticsUtils.getInstance().logAppEvent(GA_APP_OPENED);
        HSGoogleAnalyticsUtils.getInstance().logAppEvent(GA_APP_OPENED_CUSTOM_THEME_NUMBER, KCCustomThemeManager.getInstance().getAllCustomThemes().size());
    }

    private void checkIsPluginThemeInstalled() {
        if (HSSessionMgr.getCurrentSessionId() == 1) {
            HSThreadUtils.execute(new Runnable() {
                @Override
                public void run() {
                    List<PackageInfo> packages = HSApplication.getContext().getPackageManager().getInstalledPackages(0);
                    //获取主题包前缀,可能有多个
                    List<String> pluginThemePkNamePrefixList = (List<String>) HSConfig.getList("Application", "PluginTheme", "PluginThemePkNamePrefix");
                    for (int i = 0; i < packages.size(); i++) {
                        PackageInfo packageInfo = packages.get(i);
                        for (String pluginThemePkNamePrefix : pluginThemePkNamePrefixList) {
                            if (packageInfo.packageName.startsWith(pluginThemePkNamePrefix)) {
                                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("app_first_open_apk_exist", "true");
                                HSAnalytics.logEvent("app_first_open_apk_exist", "true");
                                return;
                            }
                        }
                    }
                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("app_first_open_apk_exist", "false");
                    HSAnalytics.logEvent("app_first_open_apk_exist", "false");
                }
            });
        }
    }
}

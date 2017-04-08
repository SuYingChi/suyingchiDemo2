package com.ihs.inputmethod.api;

import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.acb.interstitialads.AcbInterstitialAdManager;
import com.acb.nativeads.AcbNativeAdManager;
import com.crashlytics.android.Crashlytics;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.app.utils.HSVersionControlUtils;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.diversesession.HSDiverseSession;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSThreadUtils;
import com.ihs.inputmethod.delete.HSInputMethodApplication;
import com.ihs.inputmethod.feature.customuiratealert.CustomUIRateAlertManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.keyboardutils.nativeads.NativeAdManager;
import com.keyboard.core.themes.custom.KCCustomThemeManager;

import java.util.List;

import io.fabric.sdk.android.Fabric;

import static com.ihs.inputmethod.charging.ChargingConfigManager.PREF_KEY_USER_SET_CHARGING_TOGGLE;
import static com.ihs.inputmethod.uimodules.ui.theme.utils.Constants.GA_APP_OPENED;
import static com.ihs.inputmethod.uimodules.ui.theme.utils.Constants.GA_APP_OPENED_CUSTOM_THEME_NUMBER;

public class HSUIApplication extends HSInputMethodApplication {

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
                IAPManager.getManager().queryOwnProductIds();
            } else if (HSNotificationConstant.HS_CONFIG_CHANGED.equals(notificationName)) {
                IAPManager.getManager().onConfigChange();
            }
        }
    };

    @Override
    public void onCreate() {
        Log.e("time log", "time log application oncreated started");
        super.onCreate();
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, notificationObserver);
        //IAPManager.getManager().init()内部也会监听Session Start，由于存储监听集合的数据结构是List，因此确保HSUIApplication先接收SessionStart事件
        IAPManager.getManager().queryOwnProductIds();
        HSKeyboardThemeManager.init();

        AcbNativeAdManager.sharedInstance();

        CustomUIRateAlertManager.initialize();

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
        NativeAdManager.getInstance();
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

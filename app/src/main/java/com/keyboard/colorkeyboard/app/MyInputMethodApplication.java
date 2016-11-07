package com.keyboard.colorkeyboard.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.diversesession.HSDiverseSession;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.base.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.delete.GAConstants;
import com.ihs.inputmethod.delete.HSInputMethodApplication;
import com.ihs.inputmethod.delete.HSInputMethodCommonUtils;
import com.ihs.inputmethod.theme.api.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.ads.AdConditions;
import com.ihs.inputmethod.uimodules.ads.AdNativePoolManager;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.utils.api.HSThreadUtils;

import java.util.List;

import io.fabric.sdk.android.Fabric;

import static com.keyboard.colorkeyboard.utils.Constants.GA_APP_OPENED;
import static com.keyboard.colorkeyboard.utils.Constants.GA_APP_OPENED_CUSTOM_THEME_NUMBER;

public class MyInputMethodApplication extends HSInputMethodApplication {

    private INotificationObserver sessionEventObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_SESSION_START.equals(notificationName)) {
//                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//                if (currentapiVersion <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    HSLog.d("should delay rate alert for sdk version between 4.0 and 4.2");
//                }
                HSAlertMgr.delayRateAlert();
                onSessionStart();
            }

            if (HSNotificationConstant.HS_SESSION_END.equals(notificationName)) {
                HSDiverseSession.end();
            }
        }
    };


    @Override
    protected void onServiceCreated() {
    }

    @Override
    public void onCreate() {
        Log.e("time log", "time log application oncreated started");
        super.onCreate();
        IAPManager.getManager().initProductPrices();
        if(!HSLog.isDebugging()) {
            Fabric.with(this, new Crashlytics());//0,5s
        }
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, sessionEventObserver);
        Log.e("time log", "time log application oncreated finished");

        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    @Override
    public void onTerminate() {
        HSGlobalNotificationCenter.removeObserver(sessionEventObserver);
        unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        super.onTerminate();
    }


    private void onSessionStart() {
        //IAP 初始化,将需要购买的所有产品的product id 加入到
        IAPManager.getManager().init();
        HSDiverseSession.start();
        //检测是否已经有非内置的主题包已经被安装过了
        checkIsPluginThemeInstalled();
        if (AdConditions.isShowMainPageCardNativeAds()) {
            AdNativePoolManager.resetLastHsNativeAdShowedTime(AdNativePoolManager.YamlAdNativePool.CardAd);
            AdNativePoolManager.startAppPool(AdNativePoolManager.YamlAdNativePool.CardAd);
        }
        if (AdConditions.isShowMainThemeNativeAds()) {
            AdNativePoolManager.resetLastHsNativeAdShowedTime(AdNativePoolManager.YamlAdNativePool.ThemeAd);
            AdNativePoolManager.startAppPool(AdNativePoolManager.YamlAdNativePool.ThemeAd);
        }
        if (AdConditions.isShowThemeDetailNativeAds()) {
            AdNativePoolManager.resetLastHsNativeAdShowedTime(AdNativePoolManager.YamlAdNativePool.ThemeDetailAd);
            AdNativePoolManager.startAppPool(AdNativePoolManager.YamlAdNativePool.ThemeDetailAd);
        }
        if (AdConditions.isShowThemeTryNativeAds()) {
            AdNativePoolManager.resetLastHsNativeAdShowedTime(AdNativePoolManager.YamlAdNativePool.ThemeTryAd);
            AdNativePoolManager.startAppPool(AdNativePoolManager.YamlAdNativePool.ThemeTryAd);
        }

        HSGoogleAnalyticsUtils.getInstance().logAppEvent(GA_APP_OPENED);
        HSGoogleAnalyticsUtils.getInstance().logAppEvent(GA_APP_OPENED_CUSTOM_THEME_NUMBER,  HSKeyboardThemeManager.getCustomThemeList().size());
    }

    private void checkIsPluginThemeInstalled() {
        if(HSSessionMgr.getCurrentSessionId() == 1){
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
                                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(GAConstants.APP_FIRST_OPEN_PLUGIN_APK_EXIST,"true");
                                HSAnalytics.logEvent(GAConstants.APP_FIRST_OPEN_PLUGIN_APK_EXIST,"true");
                                return;
                            }
                        }
                    }
                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(GAConstants.APP_FIRST_OPEN_PLUGIN_APK_EXIST,"false");
                    HSAnalytics.logEvent(GAConstants.APP_FIRST_OPEN_PLUGIN_APK_EXIST,"false");
                }
            });
        }
    }

    private ActivityLifecycleCallbacks activityLifecycleCallbacks = new  ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            if(!activity.getClass().getSimpleName().equals(MainActivity.class.getSimpleName())
                    &&(!HSInputMethodCommonUtils.isCurrentIMEEnabled(activity)||!HSInputMethodCommonUtils.isCurrentIMESelected(activity))){
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };
}

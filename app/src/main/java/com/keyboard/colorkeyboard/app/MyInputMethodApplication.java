package com.keyboard.colorkeyboard.app;

import android.content.pm.PackageInfo;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.app.utils.HSVersionControlUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.diversesession.HSDiverseSession;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSUncaughtExceptionHandler;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSThreadUtils;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uninstallchecker.UninstallChecker;
import com.ihs.keyboardutils.nativeads.NativeAdManager;

import java.util.List;

import io.fabric.sdk.android.Fabric;

import static com.keyboard.colorkeyboard.utils.Constants.GA_APP_OPENED;
import static com.keyboard.colorkeyboard.utils.Constants.GA_APP_OPENED_CUSTOM_THEME_NUMBER;

public class MyInputMethodApplication extends HSApplication {

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
    public void onCreate() {
        Log.e("time log", "time log application oncreated started");
        super.onCreate();

        HSThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                HSKeyboardThemeManager.initCustomTheme();
            }
        });

        HSKeyboardThemeManager.init();

        UninstallChecker.startMonitoring(HSConfig.optString("", "Application", "UninstallFeedback", "Url"));
        HSUncaughtExceptionHandler.getInstance().init();

        IAPManager.getManager().init();

        if(!HSLog.isDebugging()) {
            Fabric.with(this, new Crashlytics());//0,5s
        }
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, sessionEventObserver);
        Log.e("time log", "time log application oncreated finished");

        if(HSVersionControlUtils.isFirstLaunchSinceInstallation()){
            ThemeAnalyticsReporter.getInstance().enableThemeAnalytics(HSKeyboardThemeManager.getCurrentTheme().mThemeName);
        }
//        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    @Override
    public void onTerminate() {
        HSGlobalNotificationCenter.removeObserver(sessionEventObserver);
//        unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        super.onTerminate();
    }


    private void onSessionStart() {
        HSDiverseSession.start();
        NativeAdManager.getInstance();
        //检测是否已经有非内置的主题包已经被安装过了
        checkIsPluginThemeInstalled();
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
                                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("app_first_open_apk_exist","true");
                                HSAnalytics.logEvent("app_first_open_apk_exist","true");
                                return;
                            }
                        }
                    }
                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("app_first_open_apk_exist","false");
                    HSAnalytics.logEvent("app_first_open_apk_exist","false");
                }
            });
        }
    }

//
//    private ActivityLifecycleCallbacks activityLifecycleCallbacks = new  ActivityLifecycleCallbacks() {
//        @Override
//        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//
//        }
//
//        @Override
//        public void onActivityStarted(Activity activity) {
//
//        }
//
//        @Override
//        public void onActivityResumed(Activity activity) {
            //do not use this code for it cause white screen
//            if(!activity.getClass().getSimpleName().equals(MainActivity.class.getSimpleName())
//                    &&(!HSInputMethodCommonUtils.isCurrentIMEEnabled(activity)||!HSInputMethodCommonUtils.isCurrentIMESelected(activity))){
//                Intent intent = new Intent(activity, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                activity.startActivity(intent);
//                activity.finish();
//            }
//        }
//
//        @Override
//        public void onActivityPaused(Activity activity) {
//
//        }
//
//        @Override
//        public void onActivityStopped(Activity activity) {
//
//        }
//
//        @Override
//        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//
//        }
//
//        @Override
//        public void onActivityDestroyed(Activity activity) {
//
//        }
//    };
}

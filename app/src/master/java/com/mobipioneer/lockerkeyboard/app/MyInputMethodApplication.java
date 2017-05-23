package com.mobipioneer.lockerkeyboard.app;

import android.content.Intent;

import com.acb.interstitialads.AcbInterstitialAdManager;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.app.utils.HSInstallationUtils;
import com.ihs.app.utils.HSMarketUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.HSUIApplication;
import com.ihs.inputmethod.api.HSUIInputMethod;
import com.ihs.inputmethod.utils.CustomUIRateAlertUtils;
import com.mobipioneer.lockerkeyboard.service.WakeKeyboardService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import static com.ihs.inputmethod.charging.ChargingConfigManager.PREF_KEY_CHARGING_NEW_USER;


public class MyInputMethodApplication extends HSUIApplication {

    private static boolean isFacebookAppInstalled = false;
    private static boolean isGooglePlayInstalled = false;


    protected INotificationObserver sessionEventObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSConfig.HS_NOTIFICATION_CONFIG_CHANGED.equals(notificationName)) {
                HSLog.d("jx,监听到HS_NOTIFICATION_CONFIG_CHANGED事件");
                HSPreferenceHelper prefs = HSPreferenceHelper.getDefault(HSApplication.getContext());
                // 如果之前没有从remote config获取到plist变化，则设置
                if (!prefs.contains(PREF_KEY_CHARGING_NEW_USER)) {
                    prefs.putBoolean(PREF_KEY_CHARGING_NEW_USER, HSConfig.optBoolean(false, "Application", "ChargeLocker", "NewUser"));
                }

            } else if (HSUIInputMethod.HS_NOTIFICATION_LOCKER_CLICK.equals(notificationName)) {
//                AppLockMgr.startLocker(HSApplication.getContext());
            } else if (HSNotificationConstant.HS_SESSION_START.equals(notificationName)) {
                HSAlertMgr.delayRateAlert();
            }
        }
    };



    @Override
    protected void onSessionStart() {
        super.onSessionStart();
        //使用新chaging机制 2017/03/03
//        setChargingFunctionStatus();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (HSConfig.optBoolean(false, "Application", "RemindChangeKeyboard", "Enable")) {
            startService(new Intent(getApplicationContext(), WakeKeyboardService.class));
        }

        AcbInterstitialAdManager.getInstance(HSApplication.getContext());

        CustomUIRateAlertUtils.initialize();

        HSGlobalNotificationCenter.addObserver(HSUIInputMethod.HS_NOTIFICATION_LOCKER_CLICK, sessionEventObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, sessionEventObserver);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(HSApplication.getContext()).build();
        ImageLoader.getInstance().init(config);

//        AppLockManager.init(this);

        isFacebookAppInstalled = HSInstallationUtils.isAppInstalled("com.facebook.katana");
        isGooglePlayInstalled = HSMarketUtils.isMarketInstalled(HSMarketUtils.GOOGLE_MARKET);

    }



    @Override
    public void onTerminate() {
        HSGlobalNotificationCenter.removeObserver(sessionEventObserver);
        super.onTerminate();
    }

    public static boolean isFacebookAppInstalled() {
        return isFacebookAppInstalled;
    }

    public static boolean isGooglePlayInstalled() {
        return isGooglePlayInstalled;
    }

}

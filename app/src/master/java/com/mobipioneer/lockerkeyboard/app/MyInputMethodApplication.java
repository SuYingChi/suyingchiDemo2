package com.mobipioneer.lockerkeyboard.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.acb.adadapter.AcbInterstitialAd;
import com.acb.interstitialads.AcbInterstitialAdLoader;
import com.acb.interstitialads.AcbInterstitialAdManager;
import com.ihs.actiontrigger.HSActionTrigger;
import com.ihs.actiontrigger.model.ActionBean;
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
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.feature.customuiratealert.CustomUIRateAlertManager;
import com.mobipioneer.lockerkeyboard.service.WakeKeyboardService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;

import static com.ihs.inputmethod.charging.ChargingConfigManager.PREF_KEY_CHARGING_NEW_USER;


public class MyInputMethodApplication extends HSUIApplication {

    private static boolean isFacebookAppInstalled = false;
    private static boolean isGooglePlayInstalled = false;
    private Intent actionService;


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

                try {
                    stopService(actionService);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    startActivity(actionService);
                } catch (Exception e) {
                    e.printStackTrace();
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

        actionService = new Intent(getApplicationContext(), HSActionTrigger.class);
        startService(actionService);
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

        startService(new Intent(getApplicationContext(), WakeKeyboardService.class));

        AcbInterstitialAdManager.getInstance(HSApplication.getContext());

        CustomUIRateAlertManager.initialize();

        HSGlobalNotificationCenter.addObserver(HSUIInputMethod.HS_NOTIFICATION_LOCKER_CLICK, sessionEventObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, sessionEventObserver);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(HSApplication.getContext()).build();
        ImageLoader.getInstance().init(config);

//        AppLockManager.init(this);

        isFacebookAppInstalled = HSInstallationUtils.isAppInstalled("com.facebook.katana");
        isGooglePlayInstalled = HSMarketUtils.isMarketInstalled(HSMarketUtils.GOOGLE_MARKET);

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
        String eventAction = "";
        switch (eventType) {
            case HSActionTrigger.EVENT_KEY_APPOPEN:
                eventAction = "ad_appOpen_Clicked";
                break;
            case HSActionTrigger.EVENT_KEY_APPQUIT:
                eventAction = "ad_appQuit_Clicked";
                break;
            case HSActionTrigger.EVENT_KEY_PHONELIGHT:
                eventAction = "ad_phoneWake_Clicked";
                break;
            case HSActionTrigger.EVENT_KEY_PHONEUNLOCK:
                eventAction = "ad_phoneUnlock_Clicked";
                break;
            case HSActionTrigger.EVENT_KEY_APPUNINSTALL:
                eventAction = "ad_appUninstall_show";
                break;
        }

        HSGoogleAnalyticsUtils.getInstance().logAppEvent(eventAction, adType + ";" + adData);
        final String eventLabel = adType + ";" + adData;
        switch (adType) {

            //full scrn ad
            case 4:
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("NativeAd_AppOpenedFullScreenAd_Load");
                List<AcbInterstitialAd> interstitialAds = AcbInterstitialAdLoader.fetch(HSApplication.getContext(), "Master_A(InterstitialAds)AppOpenedFullScreenAd", 1);
                if (interstitialAds.size() > 0) {
                    final AcbInterstitialAd interstitialAd = interstitialAds.get(0);
                    interstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
                        long adDisplayTime = -1;

                        @Override
                        public void onAdDisplayed() {
                            HSGoogleAnalyticsUtils.getInstance().logAppEvent("NativeAd_AppOpenedFullScreenAd_Show");
                            adDisplayTime = System.currentTimeMillis();
                        }

                        @Override
                        public void onAdClicked() {
                            HSGoogleAnalyticsUtils.getInstance().logAppEvent("NativeAd_AppOpenedFullScreenAd_Click");
                        }

                        @Override
                        public void onAdClosed() {
                            long duration = System.currentTimeMillis() - adDisplayTime;
                            HSGoogleAnalyticsUtils.getInstance().logAppEvent("NativeAd_AppOpenedFullScreenAd_DisplayTime", String.format("%fs", duration / 1000f));
                            interstitialAd.release();
                        }
                    });
                    interstitialAd.show();
                    return true;
                } else {
                    return false;
                }
        }
        return false;

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

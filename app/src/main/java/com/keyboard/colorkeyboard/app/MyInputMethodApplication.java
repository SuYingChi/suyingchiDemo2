package com.keyboard.colorkeyboard.app;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.diversesession.HSDiverseSession;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.HSInputMethodApplication;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;

import io.fabric.sdk.android.Fabric;

public class MyInputMethodApplication extends HSInputMethodApplication {

    private INotificationObserver sessionEventObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_SESSION_START.equals(notificationName)) {
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentapiVersion <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    HSLog.d("should delay rate alert for sdk version between 4.0 and 4.2");
                    HSAlertMgr.delayRateAlert();
                }
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
        Fabric.with(this, new Crashlytics());//0,5s
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, sessionEventObserver);
        Log.e("time log", "time log application oncreated finished");
    }

    @Override
    public void onTerminate() {
        HSGlobalNotificationCenter.removeObserver(sessionEventObserver);
        super.onTerminate();
    }


    private void onSessionStart() {
        //IAP 初始化,将需要购买的所有产品的product id 加入到
        IAPManager.getManager().init();
        HSDiverseSession.start();
    }
}

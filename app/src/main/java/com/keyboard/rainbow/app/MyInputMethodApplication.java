package com.keyboard.rainbow.app;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.diversesession.HSDiverseSession;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.HSInputMethodApplication;
import com.ihs.inputmethod.base.utils.ExecutorUtils;
import com.ihs.inputmethod.theme.HSCustomThemeDataManager;
import com.ihs.inputmethod.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.GifManager;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.HSCustomThemeContentDownloadManager;
import com.keyboard.rainbow.thread.AsyncThreadPools;
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
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        HSKeyboardThemeManager.init();
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, sessionEventObserver);
        Fresco.initialize(this);
        AsyncThreadPools.execute(new Runnable() {
            @Override
            public void run() {
                GifManager.init();
            }
        });
        HSLog.d("time log, application oncreated finished");
        MediaController.getDownloadManger().startDownloadInThreadPool(ExecutorUtils.getFixedExecutor("themeContent"));
        AsyncThreadPools.execute(new Runnable() {
            @Override
            public void run() {
                HSCustomThemeDataManager.getInstance().initCustomTheme();
                HSCustomThemeContentDownloadManager.getInstance().startDownLoadAllPreview();
            }
           });
    }

    @Override
    public void onTerminate() {
        HSGlobalNotificationCenter.removeObserver(sessionEventObserver);
        super.onTerminate();
    }

    @Override
    public void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }
    private void onSessionStart() {
        //IAP 初始化,将需要购买的所有产品的product id 加入到
        IAPManager.getManager().init();
        HSDiverseSession.start();
    }
}

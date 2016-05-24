package com.keyboard.rainbow.app;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.HSInputMethodApplication;
import com.keyboard.inputmethod.panels.KeyboardExtensionUtils;
import com.keyboard.inputmethod.panels.gif.control.GifManager;
import com.keyboard.rainbow.thread.AsyncThreadPools;

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
            }

        }
    };

    @Override
    protected void loadKeyboardPanels() {
        HSLog.d("load panels ........");
        KeyboardExtensionUtils.loadPanels();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, sessionEventObserver);
        Fresco.initialize(this);
        AsyncThreadPools.execute(new Runnable() {
            @Override
            public void run() {
                GifManager.init();
            }
        });
        HSLog.d("time log, application oncreated finished");

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
}

package com.keyboard.colorkeyboard.app;

import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.HSInputMethodApplication;
import com.keyboard.inputmethod.panels.KeyboardExtensionUtils;

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
    }

    @Override
    public void onTerminate() {
        HSGlobalNotificationCenter.removeObserver(sessionEventObserver);
        super.onTerminate();
    }
}

package com.smartkeyboard.rainbow.app;

import android.content.Context;

import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.extended.api.HSKeyboard;
import com.ihs.inputmethod.extended.eventrecorder.HSGoogleAnalyticsUtils;
import com.smartkeyboard.rainbow.R;
import com.smartkeyboard.rainbow.utils.HSFeatureUtils;
import com.smartkeyboard.rainbow.view.HSFontSelectPanel;
import com.smartkeyboard.rainbow.view.HSSettingsPanel;
import com.smartkeyboard.rainbow.view.HSThemeSelectPanel;

public class HSKeyboardApplication extends HSApplication {

    private Context mContext;
    private HSFontSelectPanel mFontSelectPanel;
    private HSSettingsPanel mSettingsPanel;
    private HSThemeSelectPanel mThemeSelectPanel;
    private static final String GA_DEBUG_TRACKER_ID = "UA-66465927-1";
    private static final String GA_RELEASE_TRACKER_ID = "UA-66468004-1";
    private static final boolean DEBUG = HSLog.isDebugging();

    private INotificationObserver loadPanelsObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle notificaiton) {
            if (eventName.equals(HSKeyboard.HS_NOTIFICATION_LOAD_APP_PANELS)) {

                HSKeyboard.getInstance().clearPanelViews();
                enableFeatures();
                mThemeSelectPanel.init();
                mFontSelectPanel.init();
                mSettingsPanel.init();
            }
        }
    };

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
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mFontSelectPanel = new HSFontSelectPanel(mContext);
        mSettingsPanel = new HSSettingsPanel(mContext);
        mThemeSelectPanel = new HSThemeSelectPanel(mContext);
        HSGlobalNotificationCenter.addObserver(HSKeyboard.HS_NOTIFICATION_LOAD_APP_PANELS, loadPanelsObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, sessionEventObserver);
        HSGoogleAnalyticsUtils.init(mContext, getTrackingId());
    }

    @Override
    public void onTerminate() {
        HSGlobalNotificationCenter.removeObserver(sessionEventObserver);
    }

    private void enableFeatures() {
        HSFeatureUtils featureUtils = HSFeatureUtils.getInstance();
        featureUtils.enableFeature(HSFeatureUtils.FEATURE_ALPHABET);

        if (mContext.getResources().getBoolean(R.bool.config_super_emoji_enabled)) {
            featureUtils.enableFeature(HSFeatureUtils.FEATURE_SUPERMOJI);
        }

        if (mContext.getResources().getBoolean(R.bool.config_sticker_enabled)) {
            featureUtils.enableFeature(HSFeatureUtils.FEATURE_STICKER);
        }
    }

    private static String getTrackingId() {
        if (DEBUG) {
            return GA_DEBUG_TRACKER_ID;
        }
        return GA_RELEASE_TRACKER_ID;
    }
}

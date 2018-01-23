package com.keyboard.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.kc.utils.KCAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.ads.fullscreen.KeyboardFullScreenAd;
import com.ihs.inputmethod.api.HSDeepLinkActivity;
import com.ihs.inputmethod.api.HSUIApplication;

import java.util.HashMap;

public class SplashActivity extends HSDeepLinkActivity {

    public static final int JUMP_TO_THEME_HOME = 1;
    public static final int JUMP_TO_CUSTOM_THEME = 2;
    public static final int JUMP_TO_FACEMOJI_CAMERA = 3;

    public static final String JUMP_TAG = "kbd_jump";

    private static final String APP_FIRST_TIME_START = "app_first_time_start";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        KeyboardFullScreenAd.canShowSessionAd = true;
        int delayMillis = 0;
        if (!HSPreferenceHelper.getDefault().getBoolean("first_start_app", true)) {
            delayMillis = HSConfig.optInteger(0, "Application", "InterstitialAds", "HomeStartDelayTime");
            KeyboardFullScreenAd.loadSessionOneTimeAd();
        } else {
            HSPreferenceHelper.getDefault().putBoolean("first_start_app", false);
        }

        super.onCreate(savedInstanceState);
        this.overridePendingTransition(0, 0);

        String openFrom;
        //launcher category indicated app open from launcher icon
        if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)) {
            openFrom = "fromIcon";
        } else {
            openFrom = "fromKeyboard";
        }
        KCAnalytics.logEvent("app_opened_new", "from", openFrom);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            HSUIApplication application = (HSUIApplication) getApplication();
            application.startActivityAfterSplash(SplashActivity.this);
        }, delayMillis);

        recordAppFirstOpen("icon click");
    }

    public static void recordAppFirstOpen(String from) {
        HSPreferenceHelper spHelper = HSPreferenceHelper.getDefault();
        if (!spHelper.contains(APP_FIRST_TIME_START)) {
            HashMap<String, String> valueMap = new HashMap<>();
            valueMap.put("from", from);

            int firstCode = HSApplication.getFirstLaunchInfo().appVersionCode;
            int currentCode = HSApplication.getCurrentLaunchInfo().appVersionCode;
            valueMap.put("firstLaunchCode", String.valueOf(firstCode));

            if (firstCode < currentCode) {
                valueMap.put("compareResult", "smaller OLD USER");
            } else if (firstCode == currentCode) {
                valueMap.put("compareResult", "equal NEW USER");
            } else if (firstCode > currentCode) {
                valueMap.put("compareResult", "greater ERROR");
            }

            KCAnalytics.logEvent(APP_FIRST_TIME_START, valueMap);
            spHelper.putBoolean(APP_FIRST_TIME_START, false);
        }
    }
}

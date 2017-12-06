package com.keyboard.common;

import android.os.Bundle;
import android.os.Handler;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.ads.fullscreen.KeyboardFullScreenAd;
import com.ihs.inputmethod.api.HSDeepLinkActivity;
import com.ihs.inputmethod.api.HSUIApplication;

public class SplashActivity extends HSDeepLinkActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(0, 0);

        int delayMillis = 0;
        if (!HSPreferenceHelper.getDefault().getBoolean("first_start_app", true)) {
            delayMillis = HSConfig.optInteger(0, "Application", "InterstitialAds", "HomeStartDelayTime");
            KeyboardFullScreenAd.loadSessionOneTimeAd();
        }else{
            HSPreferenceHelper.getDefault().putBoolean("first_start_app", false);
        }
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            HSUIApplication application = (HSUIApplication) getApplication();
            application.startActivityAfterSplash(SplashActivity.this);
            finish();
        }, delayMillis);

    }
}

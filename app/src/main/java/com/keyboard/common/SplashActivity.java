package com.keyboard.common;

import android.os.Bundle;
import android.os.Handler;

import com.ihs.inputmethod.ads.fullscreen.KeyboardFullScreenAd;
import com.ihs.inputmethod.api.HSDeepLinkActivity;
import com.ihs.inputmethod.api.HSUIApplication;

public class SplashActivity extends HSDeepLinkActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KeyboardFullScreenAd.loadSessionOneTimeAd();
        this.overridePendingTransition(0, 0);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HSUIApplication application = (HSUIApplication) getApplication();
                application.startActivityAfterSplash(SplashActivity.this);
                finish();
            }
        },4000);

    }
}

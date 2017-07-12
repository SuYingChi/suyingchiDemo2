package com.keyboard.common;

import android.os.Bundle;

import com.ihs.inputmethod.api.HSDeepLinkActivity;
import com.ihs.inputmethod.api.HSUIApplication;

public class SplashActivity extends HSDeepLinkActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HSUIApplication application = (HSUIApplication) getApplication();

        application.startActivityAfterSplash(this);

        finish();
    }
}

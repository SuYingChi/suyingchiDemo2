package com.keyboard.colorkeyboard.app;

import android.os.Bundle;

import com.ihs.inputmethod.api.HSDeepLinkActivity;
import com.ihs.inputmethod.api.HSUIApplication;

/**
 * Created by wenbinduan on 2016/11/8.
 */

public final class SplashActivity extends HSDeepLinkActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HSUIApplication application = (HSUIApplication) getApplication();

        application.startActivityAfterSplash(this);

        finish();
    }
}

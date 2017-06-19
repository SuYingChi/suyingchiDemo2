package com.keyboard.colorkeyboard.app;

import android.app.Activity;

import com.ihs.inputmethod.api.HSUIApplication;


public class MyInputMethodApplication extends HSUIApplication {
    @Override
    protected Class<? extends Activity> getMainActivityClass() {
        return MainActivity.class;
    }

    @Override
    protected Class<? extends Activity> getSplashActivityClass() {
        return SplashActivity.class;
    }
}

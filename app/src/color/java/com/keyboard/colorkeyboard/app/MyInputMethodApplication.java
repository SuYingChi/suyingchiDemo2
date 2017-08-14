package com.keyboard.colorkeyboard.app;

import android.app.Activity;

import com.ihs.inputmethod.api.HSUIApplication;
import com.keyboard.common.SplashActivity;


public class MyInputMethodApplication extends HSUIApplication {
    @Override
    protected Class<? extends Activity> getSplashActivityClass() {
        return SplashActivity.class;
    }
}

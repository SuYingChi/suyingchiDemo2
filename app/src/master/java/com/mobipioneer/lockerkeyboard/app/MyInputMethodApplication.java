package com.mobipioneer.lockerkeyboard.app;

import android.app.Activity;

import com.ihs.inputmethod.api.HSUIApplication;
import com.mobipioneer.lockerkeyboard.SplashActivity;


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

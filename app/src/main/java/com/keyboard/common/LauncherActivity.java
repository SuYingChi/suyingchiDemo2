package com.keyboard.common;

import android.os.Bundle;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.inputmethod.api.HSUIApplication;

public class LauncherActivity extends HSActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HSUIApplication application = (HSUIApplication) getApplication();

        application.startActivityAfterSplash(this);

        finish();
    }
}

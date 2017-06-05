package com.keyboard.colorkeyboard.app;

import android.content.Intent;
import android.os.Bundle;

import com.ihs.devicemonitor.accessibility.HSAccessibilityService;
import com.ihs.inputmethod.accessbility.KeyboardActivationActivity;
import com.ihs.inputmethod.accessbility.KeyboardWakeUpActivity;
import com.ihs.inputmethod.api.HSDeepLinkActivity;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by wenbinduan on 2016/11/8.
 */

public final class SplashActivity extends HSDeepLinkActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isAccessibilityEnabled = true;//HSConfig.optBoolean(false, "Application", "AutoSetKeyEnable");
        Intent intent = getIntent();//携带其他页面的数据
        if (intent == null) {
            intent = new Intent();
        }

//        intent.setClass(this, MainActivity.class);
        if (isAccessibilityEnabled) {
            if (!HSAccessibilityService.isAvailable()) {
                intent.setClass(this, KeyboardActivationActivity.class);
            } else if(!HSInputMethodListManager.isMyInputMethodSelected()){
                intent.setClass(this, KeyboardWakeUpActivity.class);
            }
        }
        startActivity(intent);
        finish();

        overridePendingTransition(R.anim.stand, R.anim.splash);
    }
}

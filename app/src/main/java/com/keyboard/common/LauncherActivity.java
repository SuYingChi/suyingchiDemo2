package com.keyboard.common;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ihs.inputmethod.utils.CommonUtils;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityInfo activityInfo = CommonUtils.querySplashActivity(this);

        if (activityInfo != null) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name));
            startActivity(intent);
        }
        finish();
    }
}

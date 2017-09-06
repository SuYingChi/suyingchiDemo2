package com.keyboard.common;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.artw.lockscreen.LockerEnableDialog;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by yanxia on 2017/8/31.
 */

public class LockerEnableActivity extends HSActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            String uri = intent.getStringExtra("lockerBgUri");
            String launchOccasion = intent.getStringExtra("launchOccasion");
            LockerEnableDialog.showLockerEnableDialog(this, uri, getString(R.string.locker_enable_title_no_desc), launchOccasion, new LockerEnableDialog.OnLockerBgLoadingListener() {
                @Override
                public void onFinish() {
                    finish();
                }
            });
        } else {
            finish();
        }
    }
}

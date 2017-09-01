package com.keyboard.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.artw.lockscreen.LockerEnableDialog;
import com.ihs.app.framework.activity.HSActivity;

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
            LockerEnableDialog.showLockerEnableDialog(this, uri, new LockerEnableDialog.OnLockerBgLoadingListener() {
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

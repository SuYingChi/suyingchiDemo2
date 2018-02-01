package com.ihs.inputmethod.uimodules.ui.customize;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.ui.customize.service.CustomizeService;
import com.ihs.inputmethod.uimodules.ui.customize.service.ICustomizeService;
import com.ihs.inputmethod.uimodules.ui.customize.service.ServiceHolder;

/**
 * Created by guonan.lv on 17/9/1.
 */

public class BaseCustomizeActivity extends HSAppCompatActivity implements ServiceConnection, ServiceHolder {
    protected ICustomizeService mService;
    protected boolean mHasPendingTheme = false;
    private boolean mIsDestroying;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind to CustomizeService
        bindCustomizeService();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = ICustomizeService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    public ICustomizeService getService() {
        return mService;
    }

    @Override
    protected void onDestroy() {
        mIsDestroying = true;
        unbindService(this);
        super.onDestroy();
    }

    protected void bindCustomizeService() {
        Intent intent = new Intent(this, CustomizeService.class);
        intent.setAction(CustomizeService.class.getName());
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public boolean isDestroying() {
//        return mIsDestroying;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    void configToolbar(String title, @ColorInt int titleColor, @ColorInt int backgroundColor) {
//        Toolbar toolbar = findViewById(R.id.action_bar);
//        if (toolbar == null) {
//            return;
//        }
//
//        toolbar.setTitle(title);
//        toolbar.setTitleTextColor(titleColor);
//        toolbar.setBackgroundColor(backgroundColor);
//        setSupportActionBar(toolbar);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getSupportActionBar().setElevation(0);
//        }
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
}

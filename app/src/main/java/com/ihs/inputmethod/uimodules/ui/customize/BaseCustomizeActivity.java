package com.ihs.inputmethod.uimodules.ui.customize;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.R;
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

    @Override
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

    public boolean isDestroying() {
        return mIsDestroying;
    }

    void configToolbar(String title, @ColorInt int titleColor, @ColorInt int backgroundColor) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        if (toolbar == null) {
            return;
        }

        toolbar.setTitle(title);
        toolbar.setTitleTextColor(titleColor);
        toolbar.setBackgroundColor(backgroundColor);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}

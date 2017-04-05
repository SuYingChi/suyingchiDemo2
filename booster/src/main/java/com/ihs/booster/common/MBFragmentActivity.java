package com.ihs.booster.common;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.WindowManager;

import com.ihs.booster.broadcast.HomeWatcherReceiver;
import com.ihs.booster.manager.MBBoostManager;
import com.ihs.booster.utils.L;

/**
 * 管理 fragment
 * Created by sharp on 16/3/8.
 */
public class MBFragmentActivity extends HSAppCompatActivity {
    protected FragmentManager fragmentManager;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(0);
            getWindow().setNavigationBarColor(0);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
    }

    @Override
    protected void onStart() {
        L.l("onStart");
        try {
            super.onStart();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        ((MBApplication) getApplication()).adDonePool.startManualPool();
//        ((MBApplication) getApplication()).chargingPool.startManualPool();
        L.line(".startManualPool();");
    }

    @Override
    protected void onRestart() {
        L.l("onRestart");
        try {
            super.onRestart();
        } catch (Exception e) {
            e.printStackTrace();
        }
        L.line("startManualPool();");
    }

    @Override
    protected void onResume() {
        L.l("onResume");
        try {
            super.onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
        HomeWatcherReceiver.registerHomeKeyReceiver();
    }

    @Override
    protected void onPause() {
        L.l("onPause");
        try {
            super.onPause();
        } catch (Exception e) {
        }
        HomeWatcherReceiver.unregisterHomeKeyReceiver();
    }

    @Override
    protected void onStop() {
        L.l("onStop");
        try {
            super.onStop();
        } catch (Exception e) {
        }
//        ((MBApplication) getApplication()).adDonePool.stopManualPool();
//        ((MBApplication) getApplication()).chargingPool.stopManualPool();
        //L.line("HSNativeAdManager.getInstance().stopManualPool();");
    }

    @Override
    protected void onDestroy() {
        L.l("onDestroy");
        try {
            super.onDestroy();
        } catch (Exception e) {
        }
        MBBoostManager.globalObserverCenter.removeObservers(this);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        L.l("requestCode:" + requestCode + " permissions:" + permissions + " grantResults:" + grantResults);
    }
}

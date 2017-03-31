package com.ihs.booster.boost.common;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.ihs.app.framework.HSApplication;
import com.ihs.booster.boost.floating.FloatBoostView;
import com.ihs.booster.utils.DisplayUtils;


public class FloatWindowManager {
    private static FloatWindowManager instance;

    private FloatBoostView floatBoostView;

    public synchronized static FloatWindowManager getInstance() {
        if (null == instance) {
            instance = new FloatWindowManager();
        }
        return instance;
    }


    public WindowManager getWindowManager() {
        WindowManager mWindowManager = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        return mWindowManager;
    }


    public void createBoostWindow() {
        final WindowManager windowManager = getWindowManager();
        if (windowManager == null) {
            return;
        }
        try {
            if (floatBoostView == null) {
                int screenWidth = DisplayUtils.getDisplayMetrics().widthPixels;
                int screenHeight = DisplayUtils.getDisplayMetrics().heightPixels;
                if (screenWidth > screenHeight) {
                    screenWidth = screenHeight + screenWidth;
                    screenHeight = screenWidth - screenHeight;
                    screenWidth = screenWidth - screenHeight;
                }
                screenHeight += DisplayUtils.getStatusBarHeight();
                floatBoostView = new FloatBoostView(HSApplication.getContext());
                LayoutParams boostWindowParams = new LayoutParams();
                boostWindowParams.type = LayoutParams.TYPE_PHONE;
                boostWindowParams.format = PixelFormat.RGBA_8888;
                boostWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                boostWindowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
                if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
                    boostWindowParams.flags |= LayoutParams.FLAG_HARDWARE_ACCELERATED;
                }
                boostWindowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                boostWindowParams.width = screenWidth;
                boostWindowParams.height = screenHeight;
                floatBoostView.setLayoutParams(boostWindowParams);
                windowManager.addView(floatBoostView, boostWindowParams);
            }
        } catch (Exception e) {
        }
    }

    public void removeBoostWindow() {
        final WindowManager windowManager = getWindowManager();
        if (windowManager == null) {
            return;
        }
        if (floatBoostView != null) {
            floatBoostView.cleanAnimation();
            try {
                windowManager.removeView(floatBoostView);
            } catch (Exception e) {
            }
        }
        floatBoostView = null;
    }


    public boolean isWindowShowing() {
        return floatBoostView != null;
    }

}

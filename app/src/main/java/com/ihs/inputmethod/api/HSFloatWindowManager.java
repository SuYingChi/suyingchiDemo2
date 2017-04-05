package com.ihs.inputmethod.api;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.ihs.app.framework.HSApplication;


public class HSFloatWindowManager {
    private static HSFloatWindowManager instance;
    private static boolean windowAdded = false;

    private View coverView;

    private Handler handler = new Handler();

    public synchronized static HSFloatWindowManager getInstance() {
        if (null == instance) {
            instance = new HSFloatWindowManager();
        }
        return instance;
    }


    public WindowManager getWindowManager() {
        return (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public void showAccessibilityCover(int layoutResID) {
        if (windowAdded) {
            return;
        }

        final WindowManager windowManager = getWindowManager();
        LayoutParams layoutParams = new LayoutParams();

        if (coverView == null) {
            coverView = View.inflate(HSApplication.getContext(), layoutResID, null);
        }

        if (isCanDrawOverlays()) {
            layoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
        } else {
            layoutParams.type = LayoutParams.TYPE_TOAST;
        }

        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags |= LayoutParams.FLAG_KEEP_SCREEN_ON | LayoutParams.FLAG_FULLSCREEN;
        layoutParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;


        try {
            windowManager.addView(coverView, layoutParams);
            windowAdded = true;
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    HSFloatWindowManager.getInstance().removeAccessibilityCover();
//                }
//            }, 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public View getCoverView() {
        return coverView;
    }

    public void removeAccessibilityCover() {
        final WindowManager windowManager = getWindowManager();
        if (coverView != null) {
            try {
                windowManager.removeViewImmediate(coverView);
                coverView = null;
                windowAdded = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isCanDrawOverlays() {
        boolean isGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                isGranted = Settings.canDrawOverlays(HSApplication.getContext());
            } catch (Exception var2) {
                var2.printStackTrace();
            }
        }
        return isGranted;
    }


}

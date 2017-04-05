package com.mobipioneer.lockerkeyboard.utils;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;


public class FloatWindowUtils {

    public static WindowManager getWindowManager() {
        WindowManager mWindowManager = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        return mWindowManager;
    }

    public static void addWindow(View view, ViewGroup.LayoutParams layoutParams) {
        final WindowManager windowManager = getWindowManager();
        if (windowManager == null) {
            return;
        }

        windowManager.addView(view, layoutParams);
    }

    public static void removeWindow(final View view) {
        final WindowManager windowManager = getWindowManager();
        if (windowManager == null) {
            return;
        }

        if (view != null) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        windowManager.removeView(view);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }
}

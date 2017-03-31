package com.ihs.booster.common.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.booster.constants.MBConfig;
import com.ihs.booster.utils.DisplayUtils;

public class MBToast {
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private View layout;
    private Handler handler = new Handler();

    public MBToast(View layoutView) {
        this.layout = layoutView;
        this.windowManager = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        initLayoutParams();
    }

    public void show() {
        windowManager.addView(layout, layoutParams);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        }, MBConfig.CLEAN_TOAST_DURATION);
    }

    private void hide() {
        windowManager.removeView(layout);
    }

    private void initLayoutParams() {
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.windowAnimations = android.R.style.Animation_Toast;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        layoutParams.y = DisplayUtils.dip2px(64);
    }
}

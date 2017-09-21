package com.ihs.inputmethod.api;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;


public class HSFloatWindowManager {
    private static HSFloatWindowManager instance;
    private static boolean windowAdded = false;

    private View coverView;
    private View floatWindow;
    private static final int MSG_REMOVE_STICKER_VIEW = 10;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REMOVE_STICKER_VIEW:
                    if (!handler.hasMessages(MSG_REMOVE_STICKER_VIEW)) {
                        removeFloatingWindow();
                    }
                    break;
            }
        }
    };

    public synchronized static HSFloatWindowManager getInstance() {
        if (null == instance) {
            instance = new HSFloatWindowManager();
        }
        return instance;
    }


    public WindowManager getWindowManager() {
        return (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public void initAccessibilityCover() {
        if (coverView == null) {
            coverView = View.inflate(HSApplication.getContext(), R.layout.layout_accessbility_cover, null);
            LayoutParams layoutParams = new LayoutParams();
            if (isCanDrawOverlays()) {
                layoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
            } else {
                layoutParams.type = LayoutParams.TYPE_TOAST;
            }

            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.flags |= LayoutParams.FLAG_KEEP_SCREEN_ON | LayoutParams.FLAG_FULLSCREEN;
            layoutParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            coverView.setLayoutParams(layoutParams);
            coverView.setAnimation(null);
        }
    }

    public void showAccessibilityCover() {
        if (windowAdded) {
            return;
        }
        final WindowManager windowManager = getWindowManager();
        try {
            HSLog.e(" cover adding ");
            windowManager.addView(coverView, coverView.getLayoutParams());
            windowAdded = true;
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

    public void showStickerSuggestionWindow(final View view, int yPos, int itemCount) {
        if (view == null) {
            return;
        }
        if (windowAdded) {
            handler.sendEmptyMessageDelayed(MSG_REMOVE_STICKER_VIEW, 5000);
            return;
        }

        LayoutParams layoutParams = new LayoutParams();
        if (isCanDrawOverlays()) {
            layoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
        } else {
            layoutParams.type = LayoutParams.TYPE_TOAST;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL;
        if (itemCount > 2) {
            layoutParams.width = DisplayUtils.dip2px(170);
        } else {
            layoutParams.width = DisplayUtils.dip2px(60) * itemCount + DisplayUtils.dip2px(20);
        }
        layoutParams.height = DisplayUtils.dip2px(80);
        layoutParams.gravity = Gravity.END | Gravity.TOP;
        layoutParams.y = yPos - DisplayUtils.dip2px(80) - DisplayUtils.dip2px(55);

        view.setLayoutParams(layoutParams);
        view.setAnimation(null);
        final WindowManager windowManager = getWindowManager();
        try {
            windowManager.addView(view, layoutParams);
            windowAdded = true;
            floatWindow = view;
            handler.sendEmptyMessageDelayed(MSG_REMOVE_STICKER_VIEW, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeFloatingWindow() {
        if (floatWindow == null || !windowAdded) {
            return;
        }
        final WindowManager windowManager = getWindowManager();
        try {
            windowManager.removeViewImmediate(floatWindow);
            windowAdded = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

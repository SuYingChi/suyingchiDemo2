package com.ihs.inputmethod.api;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;

import java.lang.reflect.InvocationTargetException;


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

    private View GameTipView;

    public synchronized static HSFloatWindowManager getInstance() {
        if (null == instance) {
            instance = new HSFloatWindowManager();
        }
        return instance;
    }


    public WindowManager getWindowManager() {
        return (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public View getAccessibilityCoverView() {
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
        return coverView;
    }

    public void showAccessibilityCover() {
        if (windowAdded) {
            return;
        }
        final WindowManager windowManager = getWindowManager();
        try {
            windowManager.addView(getAccessibilityCoverView(), getAccessibilityCoverView().getLayoutParams());
            windowAdded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

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


    public void showNewGameTipWindow(View tipView) {
        if (windowAdded) {
            return;
        }
        Resources resources = HSApplication.getContext().getResources();
        final WindowManager windowManager = getWindowManager();
        LayoutParams layoutParams = new LayoutParams();
        layoutParams.y = DisplayUtils.getScreenHeightPixels() - HSResourceUtils.getDefaultKeyboardHeight(resources) - HSResourceUtils.getDefaultSuggestionStripHeight(resources) - DisplayUtils.dip2px(102)
                - getNavigationBarSize(HSApplication.getContext()).y;


        if (isCanDrawOverlays()) {
            layoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
        } else {
            layoutParams.type = LayoutParams.TYPE_TOAST;
        }

        layoutParams.width = DisplayUtils.dip2px(239);
        layoutParams.height = DisplayUtils.dip2px(112);
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL |
                LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        this.GameTipView = tipView;
        try {
            windowManager.addView(tipView, layoutParams);
            windowAdded = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    HSFloatWindowManager.getInstance().removeGameTipView();
                }
            }, 6000);
            HSAnalytics.logEvent("keyboard_game_bubble_show");
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
            floatWindow = null;
            windowAdded = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshStickerWindowTimer() {
        handler.sendEmptyMessageDelayed(MSG_REMOVE_STICKER_VIEW, 5000);
    }

    public void removeGameTipView() {
        final WindowManager windowManager = getWindowManager();
        if (GameTipView != null) {
            try {
                windowManager.removeViewImmediate(GameTipView);
                GameTipView = null;
                windowAdded = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present
        return new Point();
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            }
        }

        return size;
    }
}

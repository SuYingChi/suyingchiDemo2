package com.ihs.inputmethod.feature.common;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.feature.lucky.LuckyActivity;
import com.ihs.inputmethod.uimodules.R;


public class ActivityUtils {

// --Commented out by Inspection START (18/1/11 下午2:41):
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public static void setCustomColorStatusBar(Activity activity, int color) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = activity.getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.setStatusBarColor(color);
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public static void setStatusBarColor(Activity activity, int color) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = activity.getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.setStatusBarColor(color);
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static void hideStatusBar(Activity activity) {
//        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static void showStatusBar(Activity activity) {
//        final Window window = activity.getWindow();
//        if ((window.getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) ==
//                WindowManager.LayoutParams.FLAG_FULLSCREEN) {
//            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public static void setNavigationBarAlpha(Activity activity, float alpha) {
//        if (CommonUtils.ATLEAST_LOLLIPOP) {
//            int alphaInt = (int) (0xff * alpha);
//            activity.getWindow().setNavigationBarColor(Color.argb(alphaInt, 0x00, 0x00, 0x00));
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static void setNavigationBarColor(Activity activity, int color) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            activity.getWindow().setNavigationBarColor(color);
//        } else {
//            setNavigationBarColorNative(activity, color);
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public static void setNavigationBarColorNative(Activity activity, int color) {
        View navigationBarView = ViewUtils.findViewById(activity, R.id.navigation_bar_bg_v);
        if (null != navigationBarView) {
            if (color == Color.TRANSPARENT) {
                navigationBarView.setVisibility(View.GONE);
            } else {
                int navigationBarHeight = CommonUtils.getNavigationBarHeight(activity);
                if (navigationBarHeight == 0) {
                    navigationBarView.setVisibility(View.GONE);
                } else {
                    InsettableFrameLayout.LayoutParams layoutParams = new InsettableFrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.ignoreInsets = true;
                    layoutParams.height = navigationBarHeight;
                    navigationBarView.setLayoutParams(layoutParams);
                    layoutParams.gravity = Gravity.BOTTOM;
                    navigationBarView.setBackgroundColor(color);
                    navigationBarView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public static void setNavigationBarColor(LuckyActivity luckyActivity, int color) {

    }

    public static void hideStatusBar(LuckyActivity luckyActivity) {

    }
}

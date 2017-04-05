package com.ihs.booster.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;

import java.lang.reflect.Field;

/**
 * Created by sharp on 16/4/7.
 */
public class DisplayUtils {
    public static DisplayMetrics getDisplayMetrics() {
        DisplayMetrics dm = new DisplayMetrics();
        getDisplay().getMetrics(dm);
        return dm;
    }

    public static Display getDisplay() {
        WindowManager mWindowManager = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        return mWindowManager.getDefaultDisplay();
    }

    public static int dip2px(int value) {
        DisplayMetrics displayMetrics = HSApplication.getContext().getResources().getDisplayMetrics();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, displayMetrics);
        return Math.round(px);
    }


    public static int px2sp(float pxValue) {
        DisplayMetrics displayMetrics = HSApplication.getContext().getResources().getDisplayMetrics();
        float fontScale = displayMetrics.scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    public static int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            statusBarHeight = HSApplication.getContext().getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

}

package com.ihs.inputmethod.feature.common;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.crashlytics.android.core.CrashlyticsCore;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    public static final String INTENT_KEY_SNAP_TO_PAGE = "snap.to.page";
    public static final String INTENT_KEY_SHOW_ALL_APPS = "show.all.apps";
    public static final String INTENT_KEY_SHOW_SEARCH_LAYOUT = "show.search.layout";
    public static final String INTENT_KEY_SHOW_SET_DEFAULT_SOURCE = "show.set.default.source";
    public static final String INTENT_KEY_WALLPAPER_SELECTED = "INTENT_KEY_WALLPAPER_SELECTED";
    public static final String INTENT_KEY_RESET_LAUNCHER_VIEW = "reset.launcher.view";

    public static final String INTENT_KEY_APPLY_THEME_OR_WALLPAPER = "apply_theme_or_wallpaper";
    public static final int INTENT_VALUE_APPLY_THEME = 1;
    public static final int INTENT_VALUE_APPLY_WALLPAPER = 2;

    public static final int DEFAULT_DEVICE_SCREEN_HEIGHT = 1920;
    public static final int DEFAULT_DEVICE_SCREEN_WIDTH = 1080;

    public static final boolean UNDER_JB_MAR2 = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2;

    public static final boolean ATLEAST_JB_MR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

    public static final boolean ATLEAST_JB_MR2 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;

    public static final boolean ATLEAST_KITKAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    public static final boolean ATLEAST_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public static final boolean ATLEAST_LOLLIPOP_MR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

    public static final boolean ATLEAST_MARSHMALLOW = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    public static final boolean ATLEAST_N = Build.VERSION.SDK_INT >= 24;

    private static float sDensityRatio;

    public static int pxFromDp(float dp) {
        return Math.round(dp * getDensityRatio());
    }

    public static float getDensityRatio() {
        if (sDensityRatio > 0f) {
            return sDensityRatio;
        }
        Resources resources = HSApplication.getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        sDensityRatio = (float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return sDensityRatio;
    }

    public static int getPhoneWidth(Context context) {
        if (null == context) {
            return DEFAULT_DEVICE_SCREEN_WIDTH;
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = DEFAULT_DEVICE_SCREEN_WIDTH;
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            if (display != null) {
                display.getMetrics(dm);
                width = dm.widthPixels;
            }
        }
        return width;
    }

    /**
     * 返回手机屏幕高度
     */
    public static int getPhoneHeight(Context context) {
        if (null == context) {
            return DEFAULT_DEVICE_SCREEN_HEIGHT;
        }
        int height = context.getResources().getDisplayMetrics().heightPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            Point localPoint = new Point();
            windowManager.getDefaultDisplay().getRealSize(localPoint);
            HSLog.v(TAG, "height == " + height + ", w == " + localPoint.x + ", h == " + localPoint.y);
            if (localPoint.y > height) {
                height = localPoint.y;
            }
        } else {
            int navigationBarHeight = CommonUtils.getNavigationBarHeight(context);
            HSLog.v(TAG, "Layout h == " + height + ", navigationBarHeight == " + navigationBarHeight);
            if (navigationBarHeight != 0 && height % 10 != 0) {
                if ((height + navigationBarHeight) % 10 == 0) {
                    height = (height + navigationBarHeight);
                }
            }
            HSLog.v(TAG, "height == " + height + ", navigationBarHeight == " + navigationBarHeight);
        }

        return height;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl() {
        Resources res = HSApplication.getContext().getResources();
        return ATLEAST_JB_MR1 && (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    /**
     * Get {@link Locale} on {@link Resources} obtained from given {@link Context}. Compatibility handled for Nougat.
     * Returns value of {@link Locale#getDefault()} when no current locale is set on given context.
     */
    @SuppressWarnings("deprecation")
    public static @NonNull
    Locale getLocale(@NonNull Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    public static boolean isMainProcess(Context context) {
        return TextUtils.equals(context.getPackageName(), HSApplication.getProcessName());
    }

    private static List<ActivityManager.RunningAppProcessInfo> getRunningProcesses(ActivityManager am) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            runningAppProcesses = new ArrayList<>(0);
        }
        return runningAppProcesses;
    }

    /**
     * @return Whether application with given package name is installed.
     * Defaults to {@code false} if error occurs when querying package manager.
     */
    public static boolean isPackageInstalled(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = HSApplication.getContext().getPackageManager().getPackageInfo(pkgName, 0);
        } catch (Exception e) {
            packageInfo = null;
        }
        return (packageInfo != null);
    }


    public static int getNavigationBarHeight(Context context) {
        if (null == context) {
            return 0;
        }
        if (context instanceof Activity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Activity activityContext = (Activity) context;
            DisplayMetrics metrics = new DisplayMetrics();
            activityContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activityContext.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight) {
                return realHeight - usableHeight;
            } else {
                return 0;
            }
        }

        return getNavigationBarHeightUnconcerned(context);
    }

    public static int getNavigationBarHeightUnconcerned(Context context) {
        if (null == context) {
            return 0;
        }
        Resources localResources = context.getResources();
        if (!hasNavBar(context)) {
            HSLog.i("no navbar");
            return 0;
        }
        int i = localResources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (i > 0) {
            return localResources.getDimensionPixelSize(i);
        }
        i = localResources.getIdentifier("navigation_bar_height_landscape", "dimen", "android");
        if (i > 0) {
            return localResources.getDimensionPixelSize(i);
        }
        return 0;
    }

    public static boolean hasNavBar(Context paramContext) {
        boolean bool = true;
        String sNavBarOverride;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Object localObject = Class.forName("android.os.SystemProperties").getDeclaredMethod("get", String.class);
                ((Method) localObject).setAccessible(true);
                sNavBarOverride = (String) ((Method) localObject).invoke(null, "qemu.hw.mainkeys");
                localObject = paramContext.getResources();
                int i = ((Resources) localObject).getIdentifier("config_showNavigationBar", "bool", "android");
                if (i != 0) {
                    bool = ((Resources) localObject).getBoolean(i);
                    if ("1".equals(sNavBarOverride)) {
                        return false;
                    }
                }
            } catch (Throwable localThrowable) {
            }
        }

        if (!ViewConfiguration.get(paramContext).hasPermanentMenuKey()) {
            return bool;
        }

        return false;
    }

    public static void startLauncher(Context context) {
        try {
            context.startActivity(getLauncherIntent());
        } catch (Exception e) {
            CrashlyticsCore.getInstance().logException(e);
            if (e instanceof ActivityNotFoundException) {
                Intent intent = new Intent();
                if (!(context instanceof Activity)) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                intent.setComponent(
                        new ComponentName("com.honeycomb.launcher", "com.honeycomb.launcher.desktop.LauncherExtension"));
                try {
                    context.startActivity(intent);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static void startLauncherWithExtra(Context context, String name, int value) {
        try {
            Intent intent = getLauncherIntent();
            intent.putExtra(name, value);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            CrashlyticsCore.getInstance().logException(e);
            Intent intent = new Intent();
            if (!(context instanceof Activity)) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.setComponent(
                    new ComponentName("com.honeycomb.launcher", "com.honeycomb.launcher.desktop.LauncherExtension"));
            intent.putExtra(name, value);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
            }
        }
    }

    public static void startLauncherAndShowAllApps(Context context) {
        Intent launcherIntent = getLauncherIntent();
        launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        launcherIntent.putExtra(INTENT_KEY_SHOW_ALL_APPS, true);
        startActivitySafely(context, launcherIntent);
    }

    public static void startLauncherAndShowSearchLayout(Context context) {
        Intent launcherIntent = getLauncherIntent();
        launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        launcherIntent.putExtra(INTENT_KEY_SHOW_SEARCH_LAYOUT, true);
        startActivitySafely(context, launcherIntent);
    }

    public static void startLauncherToPage(Context context, int page) {
        Intent launcherIntent = getLauncherIntent();
        launcherIntent.putExtra(INTENT_KEY_SNAP_TO_PAGE, page);
        startActivitySafely(context, launcherIntent);
    }

    public static Intent getLauncherIntent() {
        Intent launcherIntent = new Intent();
        launcherIntent.setAction("android.intent.action.MAIN");
        launcherIntent.addCategory("android.intent.category.HOME");
        launcherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launcherIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        launcherIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        launcherIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        launcherIntent.setPackage(LauncherConstants.LAUNCHER_PACKAGE_NAME);
        launcherIntent.putExtra(INTENT_KEY_RESET_LAUNCHER_VIEW, true);
        return launcherIntent;
    }

    private static void startActivitySafely(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException | NullPointerException ignored) {
        }
    }

    public static void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception e) {
            HSLog.e(TAG, "Error unregistering broadcast receiver: " + receiver + " at ");
            e.printStackTrace();
        }
    }
}

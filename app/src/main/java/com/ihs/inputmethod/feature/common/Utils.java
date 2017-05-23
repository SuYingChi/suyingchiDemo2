/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ihs.inputmethod.feature.common;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Debug;
import android.os.LocaleList;
import android.os.SystemClock;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

    private static final String TAG = "Launcher.Utils";

    /**
     * Threshold for float decimal equality test.
     */
    private static final float EPSILON = 0.0005f;

    private static final int STREAM_OP_BUFFER_SIZE = 4096;

    /**
     * Defines the duration in milliseconds between the first click event and
     * the second click event for an interaction to be considered a double-click.
     */
    private static final int DOUBLE_CLICK_TIMEOUT = 500;

    private static final String PREF_KEY_UNINSTALLED_APPS = "uninstalled_apps";

    private static final Rect sOldBounds = new Rect();
    private static final DrawFilter sIconDrawFilter = new PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG);

    private static final Pattern sTrimPattern = Pattern.compile("^[\\s|\\p{javaSpaceChar}]*(.*)[\\s|\\p{javaSpaceChar}]*$");

    private static int sColors[] = {0xffff0000, 0xff00ff00, 0xff0000ff};
    private static int sColorIndex = 0;

    private static final int[] sLoc0 = new int[2];
    private static final int[] sLoc1 = new int[2];

    public static final int LDPI_DEVICE_SCREEN_HEIGHT = 320;
    private static final long USE_DND_DURATION = 2 * DateUtils.HOUR_IN_MILLIS; // 2 hour don not disturb

    // To turn on these properties, type
    // adb shell setprop log.tag.PROPERTY_NAME [VERBOSE | SUPPRESS]
    private static final String FORCE_ENABLE_ROTATION_PROPERTY = "launcher_force_rotate";
    private static boolean sForceEnableRotation = isPropertyEnabled(FORCE_ENABLE_ROTATION_PROPERTY);

    public static final String ALLOW_ROTATION_PREFERENCE_KEY = "pref_allowRotation";

    private static long sLastClickTimeForDoubleClickCheck;

    // Reflection objects cache
    private static Field sReflectFieldFlagNeedsMenuKey;
    private static Field sReflectFieldNeedsMenuSetTrue;
    private static Field sReflectFieldNeedsMenuSetFalse;
    private static Method sReflectMethodSetNeedsMenuKey;

    private static long sInstallTime;

    public static boolean equals(float a, float b) {
        return Math.abs(a - b) < EPSILON;
    }

    public static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    public static long getPackageLastModifiedTime(String packageName) {
        ApplicationInfo appInfo;
        try {
            appInfo = HSApplication.getContext().getPackageManager().getApplicationInfo(packageName, 0);
        } catch (Exception e) {
            return -1;
        }
        String appFile = appInfo.sourceDir;
        return new File(appFile).lastModified();
    }

    public static List<String> getStringList(String listCsv) {
        List<String> strings = new ArrayList<>();
        for (String string : listCsv.split(",")) {
            if (!string.isEmpty()) {
                strings.add(string);
            }
        }
        return strings;
    }

    public static String getStringListCsv(List<String> strings) {
        if (strings.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(strings.get(0));
        for (int i = 1, upperBound = strings.size(); i < upperBound; i++) {
            builder.append(",").append(strings.get(i));
        }
        return builder.toString();
    }

    public static int validateIndex(List<? extends Object> sizeLimit, int rawIndex) {
        return Math.max(0, Math.min(rawIndex, sizeLimit.size() - 1));
    }

    @SuppressWarnings("deprecation")
    public static Resources getEnglishResources(Context context) {
        Configuration conf = new Configuration();
        Locale englishLocale = new Locale("en");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(englishLocale));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(englishLocale);
        } else {
            conf.locale = englishLocale;
        }
        Resources resourcesEn;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            resourcesEn = context.createConfigurationContext(conf).getResources();
        } else {
            resourcesEn = new Resources(context.getAssets(), new DisplayMetrics(), conf);
        }
        return resourcesEn;
    }

    public static final int FLASHLIGHT_STATUS_FAIL = -1;
    public static final int FLASHLIGHT_STATUS_OFF = 0;
    public static final int FLASHLIGHT_STATUS_ON = 1;


    public static boolean isWifiEnabled() {
        WifiManager wifiManager = (WifiManager) HSApplication.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public static int getDayDifference(long epoch1, long epoch2) {
        return getDayDifference(epoch1, epoch2, 0);
    }

    public static int getDayDifference(long epoch1, long epoch2, int dateLineHour) {
        Calendar wholeDayDate1 = getWholeDayCalendar(epoch1, dateLineHour);
        Calendar wholeDayDate2 = getWholeDayCalendar(epoch2, dateLineHour);

        // Get the represented date in milliseconds
        long wholeDayMillis1 = wholeDayDate1.getTimeInMillis();
        long wholeDayMillis2 = wholeDayDate2.getTimeInMillis();

        // Calculate difference in milliseconds
        long diff = Math.abs(wholeDayMillis1 - wholeDayMillis2);

        return (int) (diff / (24 * 60 * 60 * 1000));
    }

    private static Calendar getWholeDayCalendar(long epoch, int dateLineHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epoch);
        if (calendar.get(Calendar.HOUR_OF_DAY) < dateLineHour) {
            calendar.add(Calendar.DATE, -1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static boolean inSleepTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        return 6 > hourOfDay || hourOfDay >= 23;
    }

    public static boolean hasUpdate() {
        int latestVersionCode = getLatestVersionCode();
        return HSApplication.getCurrentLaunchInfo().appVersionCode < latestVersionCode;
    }

    public static int getLatestVersionCode() {
        return HSConfig.optInteger(0, "Application", "Update", "LatestVersionCode");
    }

    /**
     * Whether this launcher is set as default home screen. Defaults to {@code true} if error occurs.
     */
    public static boolean isDefaultLauncher() {
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo;
        try {
            resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        } catch (Exception e) {
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (resolveInfo != null) {
            return TextUtils.equals(HSApplication.getContext().getPackageName(), resolveInfo.activityInfo.packageName);
        }
        return true;
    }

    public static boolean isSpecialApp(String[] keywords, String packageName) {
        for (String keyword : keywords) {
            if (packageName.toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }


    public interface DefaultLauncherQueryCallback {
        void onDefaultLauncherQueryResult(boolean isDefaultLauncher);
    }

    public static List<String> getInstalledLaunchers() {
        List<String> launcherNames = new ArrayList<>();
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo info : list) {
            if (!validateLauncher(info)) {
                continue;
            }
            launcherNames.add(info.activityInfo.packageName);
        }
        return launcherNames;
    }

    private static boolean validateLauncher(ResolveInfo resolveInfo) {
        // Exclude AOSP Settings app FallbackHome activity since 7.0
        return !"com.android.settings.FallbackHome".equals(resolveInfo.activityInfo.name);
    }

    public static void setTabLayoutTypeface(final TabLayout tabLayout, final Typeface typeface) {
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                setTypefaceRecursive(tabLayout, typeface);
            }
        });
    }

    public static void setTabLayoutTextSize(final TabLayout tabLayout, final float textSize) {
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
                int tabsCount = vg.getChildCount();
                for (int j = 0; j < tabsCount; j++) {
                    ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
                    int tabChildrenCount = vgTab.getChildCount();
                    for (int i = 0; i < tabChildrenCount; i++) {
                        View tabViewChild = vgTab.getChildAt(i);
                        if (tabViewChild instanceof TextView) {
                            ((TextView) tabViewChild).setTextSize(textSize);
                        }
                    }
                }
            }
        });
    }

    public static void setTypefaceRecursive(View root, Typeface typeface) {
        if (!(root instanceof ViewGroup)) {
            if (root instanceof TextView) {
                ((TextView) root).setTypeface(typeface);
            }
            return;
        }
        int childCount = ((ViewGroup) root).getChildCount();
        for (int i = 0; i < childCount; i++) {
            setTypefaceRecursive(((ViewGroup) root).getChildAt(i), typeface);
        }
    }

    public static void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it
        View view = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Wallpaper layer must be placed under a {@link FrameLayout}.
     */
    public static void configureWallpaperLayer(Context context, View layer) {
        int top = Utils.getStatusBarHeight(context);
        int bottom = CommonUtils.getNavigationBarHeight(context);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layer.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin - top, params.rightMargin, params.bottomMargin - bottom);
        layer.setLayoutParams(params);
    }


    public static String getScreenIndexDescription(int pageIndex) {
        if (0 <= pageIndex && pageIndex <= 8) {
            return String.format(Locale.getDefault(), "Screen_%d", pageIndex + 1);
        } else if (pageIndex >= 9) {
            return "Screen_10_Or_After";
        }
        return "Invalid_Screen";
    }

    public static String getNumberDescription(int number) {
        HSLog.d("Flurry", "Number in folder: " + number);
        if (0 <= number && number <= 8) {
            return String.format(Locale.getDefault(), "Number_%d", number);
        } else if (number >= 9) {
            return "Number_9_Or_More";
        }
        return "Invalid_Number";
    }

    public static String getTimeOfDayDescription() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 24) {
            return "23~24";
        } else if (hour <= 0) {
            return "0~1";
        } else {
            return String.valueOf(hour + "~" + (hour + 1));
        }
    }

    /**
     * Sets up transparent navigation and status bars in LMP.
     * This method is a no-op for other platform versions.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setupTransparentSystemBarsForLmp(Activity activityContext) {
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            Window window = activityContext.getWindow();
            window.getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                    .SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setupTransparentSystemBarsForLmpNoNavigation(Activity activityContext) {
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            Window window = activityContext.getWindow();
            window.getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                    .SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static
    @NonNull
    Bitmap decodeResourceWithFallback(Resources res, int id) {
        Bitmap decoded = BitmapFactory.decodeResource(res, id);
        if (decoded == null) {
            decoded = createFallbackBitmap();
        }
        return decoded;
    }

    public static
    @NonNull
    Bitmap createFallbackBitmap() {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    }

    /**
     * Given a coordinate relative to the descendant, find the coordinate in a parent view's
     * coordinates.
     *
     * @param descendant        The descendant to which the passed coordinate is relative.
     * @param root              The root view to make the coordinates relative to.
     * @param outCoord          The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the descendant:
     *                          sometimes this is relevant as in a child's coordinates within the descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     * this scale factor is assumed to be equal in X and Y, and so if at any point this
     * assumption fails, we will need to return a pair of scale factors.
     */
    public static float getDescendantCoordRelativeToParent(
            View descendant, View root, int[] outCoord, boolean includeRootScroll) {
        ArrayList<View> ancestorChain = new ArrayList<>();

        float[] pt = {outCoord[0], outCoord[1]};

        View v = descendant;
        while (v != root && v != null) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        int count = ancestorChain.size();
        for (int i = 0; i < count; i++) {
            View v0 = ancestorChain.get(i);
            // For TextViews, scroll has a meaning which relates to the text position
            // which is very strange... ignore the scroll.
            if (v0 != descendant || includeRootScroll) {
                pt[0] -= v0.getScrollX();
                pt[1] -= v0.getScrollY();
            }

            v0.getMatrix().mapPoints(pt);
            pt[0] += v0.getLeft();
            pt[1] += v0.getTop();
            scale *= v0.getScaleX();
        }

        outCoord[0] = Math.round(pt[0]);
        outCoord[1] = Math.round(pt[1]);
        return scale;
    }

    /**
     */
    public static float mapCoordInSelfToDescendant(View descendant, View root, int[] coord) {
        ArrayList<View> ancestorChain = new ArrayList<>();

        float[] pt = {coord[0], coord[1]};

        View v = descendant;
        while (v != root) {
            if (v == null) {
                // FIXME: view should not be orphaned here
                break;
            }
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        Matrix inverse = new Matrix();
        int count = ancestorChain.size();
        for (int i = count - 1; i >= 0; i--) {
            View ancestor = ancestorChain.get(i);
            View next = i > 0 ? ancestorChain.get(i - 1) : null;

            pt[0] += ancestor.getScrollX();
            pt[1] += ancestor.getScrollY();

            if (next != null) {
                pt[0] -= next.getLeft();
                pt[1] -= next.getTop();
                next.getMatrix().invert(inverse);
                inverse.mapPoints(pt);
                scale *= next.getScaleX();
            }
        }

        coord[0] = Math.round(pt[0]);
        coord[1] = Math.round(pt[1]);
        return scale;
    }

    /**
     * Utility method to determine whether the given point, in local coordinates,
     * is inside the view, where the area of the view is expanded by the slop factor.
     * This method is called while processing touch-move events to determine if the event
     * is still within the view.
     */
    public static boolean pointInView(View v, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < (v.getWidth() + slop) &&
                localY < (v.getHeight() + slop);
    }

    public static void scaleRect(Rect r, float scale) {
        if (scale != 1.0f) {
            r.left = (int) (r.left * scale + 0.5f);
            r.top = (int) (r.top * scale + 0.5f);
            r.right = (int) (r.right * scale + 0.5f);
            r.bottom = (int) (r.bottom * scale + 0.5f);
        }
    }

    @Deprecated
    public static int[] getCenterDeltaInScreenSpace(View v0, int[] delta) {
        v0.getLocationInWindow(sLoc0);
        sLoc0[1] += (v0.getMeasuredHeight() * v0.getScaleY()) / 2;

        if (delta == null) {
            delta = new int[2];
        }

        delta[0] = 0;
        delta[1] = sLoc0[1] / 2;

        return delta;
    }

    public static void scaleRectAboutCenter(Rect r, float scale) {
        int cx = r.centerX();
        int cy = r.centerY();
        r.offset(-cx, -cy);
        Utils.scaleRect(r, scale);
        r.offset(cx, cy);
    }

    public static boolean isSystemApp(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        ComponentName cn = intent.getComponent();
        String packageName = null;
        if (cn == null) {
            ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if ((info != null) && (info.activityInfo != null)) {
                packageName = info.activityInfo.packageName;
            }
        } else {
            packageName = cn.getPackageName();
        }
        if (packageName != null) {
            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                return (info != null) && (info.applicationInfo != null) &&
                        ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            } catch (NameNotFoundException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isSystemApp(ApplicationInfo appInfo) {
        return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
    }

    /**
     * @return Default to {@code false} on error.
     */
    public static boolean isSystemApp(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        if (packageName.contains("com.google") || packageName.contains("com.android") || packageName.contains("android.process")) {
            return true;
        }
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            return null != applicationInfo && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isLaunchAbleApp(Context context, String packageName) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);
        return null != context.getPackageManager().resolveActivity(intent, 0);
    }

    /**
     * This picks a dominant color, looking for high-saturation, high-value, repeated hues.
     *
     * @param bitmap  The bitmap to scan
     * @param samples The approximate max number of samples to use.
     */
    public static int findDominantColorByHue(Bitmap bitmap, int samples) {
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        int sampleStride = (int) Math.sqrt((height * width) / samples);
        if (sampleStride < 1) {
            sampleStride = 1;
        }

        // This is an out-param, for getting the hsv values for an rgb
        float[] hsv = new float[3];

        // First get the best hue, by creating a histogram over 360 hue buckets,
        // where each pixel contributes a score weighted by saturation, value, and alpha.
        float[] hueScoreHistogram = new float[360];
        float highScore = -1;
        int bestHue = -1;

        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int argb = bitmap.getPixel(x, y);
                int alpha = 0xFF & (argb >> 24);
                if (alpha < 0x80) {
                    // Drop mostly-transparent pixels.
                    continue;
                }
                // Remove the alpha channel.
                int rgb = argb | 0xFF000000;
                Color.colorToHSV(rgb, hsv);
                // Bucket colors by the 360 integer hues.
                int hue = (int) hsv[0];
                if (hue < 0 || hue >= hueScoreHistogram.length) {
                    // Defensively avoid array bounds violations.
                    continue;
                }
                float score = hsv[1] * hsv[2];
                hueScoreHistogram[hue] += score;
                if (hueScoreHistogram[hue] > highScore) {
                    highScore = hueScoreHistogram[hue];
                    bestHue = hue;
                }
            }
        }

        SparseArray<Float> rgbScores = new SparseArray<>();
        int bestColor = 0xff000000;
        highScore = -1;
        // Go back over the RGB colors that match the winning hue,
        // creating a histogram of weighted s*v scores, for up to 100*100 [s,v] buckets.
        // The highest-scoring RGB color wins.
        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int rgb = bitmap.getPixel(x, y) | 0xff000000;
                Color.colorToHSV(rgb, hsv);
                int hue = (int) hsv[0];
                if (hue == bestHue) {
                    float s = hsv[1];
                    float v = hsv[2];
                    int bucket = (int) (s * 100) + (int) (v * 10000);
                    // Score by cumulative saturation * value.
                    float score = s * v;
                    Float oldTotal = rgbScores.get(bucket);
                    float newTotal = oldTotal == null ? score : oldTotal + score;
                    rgbScores.put(bucket, newTotal);
                    if (newTotal > highScore) {
                        highScore = newTotal;
                        // All the colors in the winning bucket are very similar. Last in wins.
                        bestColor = rgb;
                    }
                }
            }
        }
        return bestColor;
    }

    /*
     * Finds a system apk which had a broadcast receiver listening to a particular action.
     * @param action intent action used to find the apk
     * @return a pair of apk package name and the resources.
     */
    public static Pair<String, Resources> findSystemApk(String action, PackageManager pm) {
        final Intent intent = new Intent(action);
        for (ResolveInfo info : pm.queryBroadcastReceivers(intent, 0)) {
            if (info.activityInfo != null && (info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                final String packageName = info.activityInfo.packageName;
                try {
                    final Resources res = pm.getResourcesForApplication(packageName);
                    return Pair.create(packageName, res);
                } catch (NameNotFoundException e) {
                    HSLog.w(TAG, "Failed to find resources for " + packageName);
                }
            }
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isViewAttachedToWindow(View v) {
        if (CommonUtils.ATLEAST_KITKAT) {
            return v.isAttachedToWindow();
        } else {
            // A proxy call which returns null, if the view is not attached to the window.
            return v.getKeyDispatcherState() != null;
        }
    }

    /**
     * Returns a widget with category {@link AppWidgetProviderInfo#WIDGET_CATEGORY_SEARCHBOX}
     * provided by the same package which is set to be global search activity.
     * If widgetCategory is not supported, or no such widget is found, returns the first widget
     * provided by the package.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static AppWidgetProviderInfo getSearchWidgetProvider(Context context) {
        SearchManager searchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        ComponentName searchComponent = searchManager.getGlobalSearchActivity();
        if (searchComponent == null)
            return null;
        String providerPkg = searchComponent.getPackageName();

        AppWidgetProviderInfo defaultWidgetForSearchPackage = null;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        for (AppWidgetProviderInfo info : appWidgetManager.getInstalledProviders()) {
            if (info.provider.getPackageName().equals(providerPkg)) {
                if (CommonUtils.ATLEAST_JB_MR1) {
                    if ((info.widgetCategory & AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX) != 0) {
                        return info;
                    } else if (defaultWidgetForSearchPackage == null) {
                        defaultWidgetForSearchPackage = info;
                    }
                } else {
                    return info;
                }
            }
        }
        return defaultWidgetForSearchPackage;
    }

    /**
     * Compresses the bitmap to a byte array for serialization.
     */
    public static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            HSLog.w(TAG, "Could not write bitmap");
            return null;
        }
    }

    /**
     * Find the first vacant cell, if there is one.
     *
     * @param vacant Holds the x and y coordinate of the vacant cell
     * @param spanX  Horizontal cell span.
     * @param spanY  Vertical cell span.
     * @return true if a vacant cell was found
     */
    public static boolean findVacantCell(int[] vacant, int spanX, int spanY, int xCount, int yCount, boolean[][] occupied) {

        for (int y = 0; (y + spanY) <= yCount; y++) {
            for (int x = 0; (x + spanX) <= xCount; x++) {
                boolean available = !occupied[x][y];
                out:
                for (int i = x; i < x + spanX; i++) {
                    for (int j = y; j < y + spanY; j++) {
                        available = available && !occupied[i][j];
                        if (!available)
                            break out;
                    }
                }

                if (available) {
                    vacant[0] = x;
                    vacant[1] = y;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Trims the string, removing all whitespace at the beginning and end of the string.
     * Non-breaking whitespaces are also removed.
     */
    public static String trim(CharSequence s) {
        if (s == null) {
            return null;
        }

        // Just strip any sequence of whitespace or java space characters from the beginning and end
        Matcher m = sTrimPattern.matcher(s);
        return m.replaceAll("$1");
    }

    /**
     * Calculates the height of a given string at a specific text size.
     */
    public static float calculateTextHeight(float textSizePx) {
        Paint p = new Paint();
        p.setTextSize(textSizePx);
        Paint.FontMetrics fm = p.getFontMetrics();
        return -fm.top + fm.bottom;
    }

    /**
     * Convenience println with multiple args.
     */
    public static void println(String key, Object... args) {
        StringBuilder b = new StringBuilder();
        b.append(key);
        b.append(": ");
        boolean isFirstArgument = true;
        for (Object arg : args) {
            if (isFirstArgument) {
                isFirstArgument = false;
            } else {
                b.append(", ");
            }
            b.append(arg);
        }
        System.out.println(b.toString());
    }

    public static float dpiFromPx(int size) {
        return (size / CommonUtils.getDensityRatio());
    }

    public static int pxFromDp(float size, DisplayMetrics metrics) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, metrics));
    }

    public static int pxFromSp(float size, DisplayMetrics metrics) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, metrics));
    }

    public static String createDbSelectionQuery(String columnName, Iterable<?> values) {
        return String.format(Locale.ENGLISH, "%s IN (%s)", columnName, TextUtils.join(", ", values));
    }

    /**
     * @return 0 for failure.
     */
    public static int getVersionCode() {
        Context context = HSApplication.getContext();
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getVersionName() {
        Context context = HSApplication.getContext();
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Height with status bar but not navigation bar
     */
    public static Point getScreenSize(Activity launcher) {
        Display display = launcher.getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        return screenSize;
    }

    /**
     * @return Status bar (top bar) height. Note that this height remains fixed even when status bar is hidden.
     */
    public static int getStatusBarHeight(Context context) {
        if (null == context) {
            return 0;
        }
        int height = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }


    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static void showNavigationBarMenuButton(Activity activity) {
        if (!CommonUtils.hasNavBar(activity))
            return;
        int menuFlag;
        try {
            if (sReflectFieldFlagNeedsMenuKey == null) {
                sReflectFieldFlagNeedsMenuKey = WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY");
            }
            menuFlag = sReflectFieldFlagNeedsMenuKey.getInt(null);
            Window window = activity.getWindow();
            window.addFlags(menuFlag);
            return;
        } catch (Exception ignored) {
        }

        try {
            if (sReflectFieldNeedsMenuSetTrue == null) {
                sReflectFieldNeedsMenuSetTrue = WindowManager.LayoutParams.class.getField("NEEDS_MENU_SET_TRUE");
            }
            menuFlag = sReflectFieldNeedsMenuSetTrue.getInt(null);
            if (sReflectMethodSetNeedsMenuKey == null) {
                sReflectMethodSetNeedsMenuKey = Window.class.getDeclaredMethod("setNeedsMenuKey", int.class);
            }
            sReflectMethodSetNeedsMenuKey.setAccessible(true);
            sReflectMethodSetNeedsMenuKey.invoke(activity.getWindow(), menuFlag);
        } catch (Exception ignored) {
        }
    }

    public static void hideNavigationBarMenuButton(Activity activity) {
        if (!CommonUtils.hasNavBar(activity))
            return;
        int menuFlag;
        try {
            if (sReflectFieldFlagNeedsMenuKey == null) {
                sReflectFieldFlagNeedsMenuKey = WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY");
            }
            menuFlag = sReflectFieldFlagNeedsMenuKey.getInt(null);
            Window window = activity.getWindow();
            window.clearFlags(menuFlag);
            return;
        } catch (Exception ignored) {
        }
        try {
            if (sReflectFieldNeedsMenuSetFalse == null) {
                sReflectFieldNeedsMenuSetFalse = WindowManager.LayoutParams.class.getField("NEEDS_MENU_SET_FALSE");
            }
            menuFlag = sReflectFieldNeedsMenuSetFalse.getInt(null);
            if (sReflectMethodSetNeedsMenuKey == null) {
                sReflectMethodSetNeedsMenuKey = Window.class.getDeclaredMethod("setNeedsMenuKey", int.class);
            }
            sReflectMethodSetNeedsMenuKey.setAccessible(true);
            sReflectMethodSetNeedsMenuKey.invoke(activity.getWindow(), menuFlag);
        } catch (Exception ignored) {
        }
    }

    /**
     * Check if network of given type is currently available.
     *
     * @param type one of {@link ConnectivityManager#TYPE_MOBILE}, {@link ConnectivityManager#TYPE_WIFI},
     *             {@link ConnectivityManager#TYPE_WIMAX}, {@link ConnectivityManager#TYPE_ETHERNET},
     *             {@link ConnectivityManager#TYPE_BLUETOOTH}, or other types defined by {@link ConnectivityManager}.
     *             Pass -1 for ANY type
     */
    public static boolean isNetworkAvailable(int type) {
        Context context = HSApplication.getContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        } else if (CommonUtils.ATLEAST_LOLLIPOP) {
            return isNetworkAvailableLollipop(cm, type);
        } else {
            return isNetworkAvailableJellyBean(cm, type);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean isNetworkAvailableLollipop(ConnectivityManager cm, int type) {
        try {
            Network[] networks = cm.getAllNetworks();
            if (networks != null) {
                for (Network network : networks) {
                    NetworkInfo networkInfo = cm.getNetworkInfo(network);
                    if (networkInfo != null && networkInfo.getState() != null && isTypeMatchAndConnected(networkInfo, type)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static boolean isNetworkAvailableJellyBean(ConnectivityManager cm, int type) {
        try {
            NetworkInfo[] networkInfos = cm.getAllNetworkInfo();
            if (networkInfos != null) {
                for (NetworkInfo networkInfo : networkInfos) {
                    if (networkInfo.getState() != null && isTypeMatchAndConnected(networkInfo, type)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isTypeMatchAndConnected(@NonNull NetworkInfo networkInfo, int type) {
        return (type == -1 || networkInfo.getType() == type) && networkInfo.isConnected();
    }

    /**
     * If either memory cache or disk cache of {@link ImageLoader} exists for given image.
     *
     * @param url URL to load image
     */
    public static boolean imageCacheExists(@NonNull String url) {
        ImageLoader loader = ImageLoader.getInstance();
        if (loader.getMemoryCache().get(url) != null) {
            return true;
        }
        File file = loader.getDiscCache().get(url);
        if (file == null) {
            return false;
        }
        return file.exists();
    }

    public static byte[] readFile(File file) {
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[STREAM_OP_BUFFER_SIZE];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        } catch (IOException e) {
            return new byte[0];
        } finally {
            try {
                if (ous != null) {
                    ous.close();
                }
            } catch (IOException ignored) {
            }
            try {
                if (ios != null) {
                    ios.close();
                }
            } catch (IOException ignored) {
            }
        }
        return ous.toByteArray();
    }

    public static void writeToFile(File file, byte[] data) {
        FileOutputStream fos = null;
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    HSLog.d(TAG, "Create file " + file.getAbsolutePath());
                }
            }
            fos = new FileOutputStream(file);
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public static void saveBitmapToFile(Bitmap bitmap, String fileOutPath, int quality) {
        try {
            saveBitmapToFileInternal(bitmap, new FileOutputStream(fileOutPath), quality);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveBitmapToFile(Bitmap bitmap, File fileOutPath, int quality) {
        try {
            saveBitmapToFileInternal(bitmap, new FileOutputStream(fileOutPath), quality);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void saveBitmapToFileInternal(Bitmap bitmap, FileOutputStream fos, int quality) {
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean saveInputStreamToFile(InputStream is, File fileOut) {
        return saveInputStreamToFile(null, is, fileOut);
    }

    public static boolean saveInputStreamToFile(byte[] preData, InputStream is, File fileOut) {
        OutputStream output = null;
        try {
            output = new FileOutputStream(fileOut);
            if (null != preData) {
                output.write(preData);
            }

            byte[] buffer = new byte[STREAM_OP_BUFFER_SIZE];
            int read;

            while ((read = is.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
                if (output != null) {
                    output.close();
                }
            } catch (IOException ignored) {
            }
        }
        return true;
    }

    /**
     * Retrieve, creating if needed, a new directory of given name in which we
     * can place our own custom data files.
     */
    public static
    @Nullable
    File getDirectory(String dirPath) {
        File file = HSApplication.getContext().getFilesDir();
        String[] path = dirPath.split(File.separator);
        for (String dir : path) {
            file = new File(file, dir);
            if (!file.exists() && !file.mkdir()) {
                HSLog.w(TAG, "Error making directory");
                return null;
            }
        }
        return file;
    }

    /**
     * Retrieve, creating if needed, a new sub-directory in cache directory.
     * Internal cache directory is used if external cache directory is not available.
     */
    public static File getCacheDirectory(String subDirectory) {
        return getCacheDirectory(subDirectory, false);
    }

    /**
     * @param useInternal Only uses internal cache directory when {@code true}.
     */
    public static File getCacheDirectory(String subDirectory, boolean useInternal) {
        Context context = HSApplication.getContext();
        String cacheDirPath;
        File externalCache = null;
        if (!useInternal) {
            try {
                externalCache = context.getExternalCacheDir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (externalCache != null) {
            cacheDirPath = externalCache.getAbsolutePath() + File.separator + subDirectory + File.separator;
        } else {
            cacheDirPath = context.getCacheDir().getAbsolutePath() + File.separator + subDirectory + File.separator;
        }
        File cacheDir = new File(cacheDirPath);
        if (!cacheDir.exists()) {
            if (cacheDir.mkdirs()) {
                HSLog.d("Utils.Cache", "Created cache directory: " + cacheDir.getAbsolutePath());
            } else {
                HSLog.e("Utils.Cache", "Failed to create cache directory: " + cacheDir.getAbsolutePath());
            }
        }
        return cacheDir;
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        boolean success = fileOrDirectory.delete();
        HSLog.v("Launcher.Files", "Delete " + (fileOrDirectory.isDirectory() ? "directory " : "file ")
                + fileOrDirectory.getName() + ", success: " + success);
    }

    public static int[] getUnitSizeInRecyclerView(View unit) {
        int[] size = new int[2];
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) unit.getLayoutParams();
        size[0] = unit.getWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
        size[1] = unit.getHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
        return size;
    }

    public static long getMemoryTotalSize() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            initial_memory = Long.valueOf(arrayOfString[1]) * 1024;
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return initial_memory;
    }

    public static long getMemoryAvailableSize() {
        ActivityManager am = (ActivityManager) HSApplication.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem + getSelfMemoryUsed();
    }

    private static long getSelfMemoryUsed() {
        long memSize = 0;
        ActivityManager am = (ActivityManager) HSApplication.getContext().getSystemService(HSApplication.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAPP : runningApps) {
            if (HSApplication.getContext().getPackageName().equals(runningAPP.processName)) {
                int[] pids = new int[]{runningAPP.pid};
                Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(pids);
                memSize = memoryInfo[0].getTotalPss() * 1024;
                break;
            }
        }
        return memSize;
    }

    public static String getRemoteFileExtension(String url) {
        String extension = "";
        if (url != null) {
            int i = url.lastIndexOf('.');
            int p = Math.max(url.lastIndexOf('/'), url.lastIndexOf('\\'));
            if (i > p) {
                extension = url.substring(i + 1);
            }
        }
        return extension;
    }

    /**
     * @return {@code n} unique integers in range [start, end). Or {@code null} when end - start < n
     * or end <= start. Note that this implementation is for "dense" params, where end - start is no
     * larger than a reasonable size of array list allocation, and where n takes a substantial
     * portion of the range.
     */
    public static int[] getUniqueRandomInts(int start, int end, int n) {
        if (n > end - start || end <= start) {
            return null;
        }
        List<Integer> numberList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            numberList.add(i);
        }
        Collections.shuffle(numberList);
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = numberList.get(i);
        }
        return result;
    }

    public static float formatNumberOneDigit(double number) {
        return (float) (Math.round(number * 10)) / 10;
    }

    public static double formatNumberTwoDigit(double number) {
        BigDecimal bg = new BigDecimal(number);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @MainThread
    public static boolean checkDoubleClickGlobal() {
        long time = SystemClock.elapsedRealtime();
        long timeD = time - sLastClickTimeForDoubleClickCheck;
        if (0 < timeD && timeD < DOUBLE_CLICK_TIMEOUT) {
            return true;
        }
        sLastClickTimeForDoubleClickCheck = time;
        return false;
    }

    public static boolean isKeyguardLocked(Context context, boolean defaultValue) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        try {
            Method declaredMethod = KeyguardManager.class.getDeclaredMethod("isKeyguardLocked");
            declaredMethod.setAccessible(true);
            defaultValue = (Boolean) declaredMethod.invoke(keyguardManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
        return defaultValue;
    }

    public static String getAppLabel(String packageName) {
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (Exception ignored) {
        }
        return (String) ((null != applicationInfo) ? packageManager.getApplicationLabel(applicationInfo) : "");
    }

    public static void setAppBarScrollStatus(CollapsingToolbarLayout collapsingToolbarLayout, boolean enable) {
        if (null == collapsingToolbarLayout) {
            return;
        }
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(enable ? (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) : 0);
        collapsingToolbarLayout.setLayoutParams(params);
    }

    public static boolean isNewUser() {
        return HSApplication.getFirstLaunchInfo().appVersionCode == HSApplication.getCurrentLaunchInfo().appVersionCode;
    }

    public static File getDiscCacheFile(String url) {
        if (ImageLoader.getInstance().getDiskCache() != null && !TextUtils.isEmpty(url)) {
            File file = ImageLoader.getInstance().getDiskCache().get(url);
            return file;
        }
        return null;
    }

    public static boolean hasDiscCache(String url) {
        File file = getDiscCacheFile(url);
        return checkFileValid(file);
    }

    public static boolean checkFileValid(File file) {
        if (file != null && file.exists()) {
            return true;
        }
        return false;
    }
}

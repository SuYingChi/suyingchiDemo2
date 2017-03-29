package com.ihs.booster.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.booster.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by sharp on 15/8/12.
 */
public class Utils {
    public static void broughtAppToFront() {
        Intent intent = HSApplication.getContext().getPackageManager().getLaunchIntentForPackage(HSApplication.getContext()
                .getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        HSApplication.getContext().startActivity(intent);
    }


    public static int getVersionCode() {
        try {
            PackageInfo pi = HSApplication.getContext().getPackageManager().getPackageInfo(HSApplication.getContext().getPackageName(), 0);
            return pi.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getVersionName() {
        try {
            PackageInfo pi = HSApplication.getContext().getPackageManager().getPackageInfo(HSApplication.getContext().getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static DisplayMetrics getDisplayMetrics() {
        DisplayMetrics dm = new DisplayMetrics();
        getDisplay().getMetrics(dm);
        return dm;
    }

    public static Display getDisplay() {
        WindowManager mWindowManager = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        return mWindowManager.getDefaultDisplay();
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

    public static Intent createEmailIntent(final String toEmail, final String subject, final String message) {
        Intent sendTo = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode(toEmail) + "?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(message);
        Uri uri = Uri.parse(uriText);
        sendTo.setData(uri);
        List<ResolveInfo> resolveInfos = HSApplication.getContext().getPackageManager().queryIntentActivities(sendTo, 0);
        if (!resolveInfos.isEmpty()) {
            return sendTo;
        }
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_EMAIL, new String[]{toEmail});
        send.putExtra(Intent.EXTRA_SUBJECT, subject);
        send.putExtra(Intent.EXTRA_TEXT, message);
        return send;
    }


    public static int getSDUsedPercent() {
        long sdTotalSize = getSDTotalSize();
        if (sdTotalSize == 0) {
            return 0;
        } else {
            Long percent = 100 - getSDAvailableSize() * 100 / (sdTotalSize);
            return percent.intValue();
        }
    }

    /**
     * 手机 SD 卡总空间
     */
    public static long getSDTotalSize() {
        try {
            File externalSD = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                externalSD = Environment.getExternalStorageDirectory();
            }
            if (externalSD != null) {
                return externalSD.getTotalSpace();
            }
        } catch (Exception e) {

        }
        return 0;
    }

    /**
     * app System Data size
     */
    public static long getDataTotalSize() {
        try {
            StatFs sf = new StatFs(HSApplication.getContext().getCacheDir().getAbsolutePath());
            long blockSize = sf.getBlockSize();
            long totalBlocks = sf.getBlockCount();
            return blockSize * totalBlocks;
        } catch (Exception e) {
            return 0l;
        }
    }

    /**
     * 手机 SD 卡可用空间
     */
    public static long getSDAvailableSize() {
        try {
            File externalSD = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                externalSD = Environment.getExternalStorageDirectory();
            }
            if (externalSD != null) {
                return externalSD.getUsableSpace();
            }
        } catch (Exception e) {

        }
        return 0;
    }


    public static boolean isShortcutExist() {
        Context context = HSApplication.getContext();
        boolean result = false;
        // 获取当前应用名称
        try {
            String title = context.getString(R.string.app_shortcut_name);
            final String uriStr = "content://" + getAuthorityFromPermission("launcher.permission.READ_SETTINGS") + "/favorites?notify=true";
            final Uri CONTENT_URI = Uri.parse(uriStr);
            final Cursor c = context.getContentResolver().query(CONTENT_URI, null, "title=?", new String[]{title}, null);
            if (c != null && c.getCount() > 0) {
                c.close();
                result = true;
            }
        } catch (Exception e) {
            L.l("Exception:" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    private static String getAuthorityFromPermission(String permission) {
        Context context = HSApplication.getContext();
        String authority = "";
        if (TextUtils.isEmpty(permission)) {
            return authority;
        }
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
        if (packs != null) {
            for (PackageInfo pack : packs) {
                ProviderInfo[] providers = pack.providers;
                if (providers != null) {
                    for (ProviderInfo provider : providers) {
                        if (!TextUtils.isEmpty(provider.readPermission) && provider.readPermission.contains(permission)) {
                            authority = provider.authority;
                            return authority;
                        }
                    }
                }
            }
        }
        return authority;
    }

    public static void runOnUiThread(Runnable action) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            action.run();
        } else {
            new Handler(Looper.getMainLooper()).post(action);
        }
    }

    public static int getScreenWidthPixels() {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScreenHeightPixels() {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(dm);
        return dm.heightPixels;
    }

    public static float temperatureConvert2Fahrenheit(float temp) {
        return 32.0F + 1.8F * temp;
    }
}

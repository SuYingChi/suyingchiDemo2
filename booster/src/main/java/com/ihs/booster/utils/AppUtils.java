package com.ihs.booster.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.booster.R;
import com.ihs.booster.boost.common.viewdata.BoostApp;
import com.ihs.booster.boost.memory.MemoryPrefsManager;
import com.ihs.booster.utils.processes.AndroidAppProcess;
import com.ihs.booster.utils.processes.ProcessUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by zhixiangxiao on 15/11/24.
 */
public class AppUtils {
    public static String getAppName(String packageName) {
        ApplicationInfo applicationInfo = null;
        String appName = "";
        try {
            applicationInfo = HSApplication.getContext().getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            appName = HSApplication.getContext().getPackageManager().getApplicationLabel(applicationInfo).toString();
            appName = appName.trim().replace(" ", "");
        } catch (Exception e) {
            appName = "";
        }
        if (TextUtils.equals(packageName, appName)) {
            appName = "";
        }
        return appName;
    }

    public static Drawable getAppIcon(String packageName) {
        Drawable icon = HSApplication.getContext().getResources().getDrawable(R.mipmap.app_icon_default);
        try {
            icon = HSApplication.getContext().getPackageManager().getApplicationIcon(packageName);
        } catch (Exception e) {
        }
        return icon;
    }

    public static Drawable getApkIcon(String apkFilePath) {
        Drawable apkIcon = HSApplication.getContext().getResources().getDrawable(R.mipmap.app_icon_default);
        try {
            PackageManager pm = HSApplication.getContext().getPackageManager();
            PackageInfo pi = pm.getPackageArchiveInfo(apkFilePath, 0);
            pi.applicationInfo.sourceDir = apkFilePath;
            pi.applicationInfo.publicSourceDir = apkFilePath;
            apkIcon = pi.applicationInfo.loadIcon(pm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apkIcon;
    }

    public static boolean isAppInstalled(String packageName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = HSApplication.getContext().getPackageManager().getPackageInfo(packageName, 0);
        } catch (Exception e) {
        }
        return packageInfo != null;
    }

    public static boolean isSystemApp(String packageName) {
        boolean is_system_app;
        try {
            if (Build.DEVICE.contains("huawei") || Build.DEVICE.contains("HUAWEI")) {
                is_system_app = isSystemAppHuaWei(packageName);
            } else {
                is_system_app = isSystemAppNormal(packageName);
            }
        } catch (Exception e) {
            is_system_app = false;
        }
        return is_system_app;
    }

    private static boolean isSystemAppNormal(String mPackageName) {
        boolean is_system_app;
        try {
            ApplicationInfo applicationInfo = HSApplication.getContext().getPackageManager().getApplicationInfo
                    (mPackageName, PackageManager.GET_META_DATA);
            is_system_app = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
        } catch (NameNotFoundException e) {
            is_system_app = false;
        }
        return is_system_app;
    }

    // todo 调查如何区分华为手机
    private static boolean isSystemAppHuaWei(String mPackageName) {
        boolean is_system_app = true;
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = HSApplication.getContext().getPackageManager().getApplicationInfo
                    (mPackageName, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (applicationInfo != null) {
            try {
                Method[] methods = applicationInfo.getClass().getMethods();
                for (Method method1 : methods) {
                    method1.setAccessible(true);
                    if (method1.getReturnType() == String.class) {
                        try {
                            String returnValue = (String) method1.invoke(applicationInfo);
                            HSLog.d(method1.getName() + ":" + returnValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                            HSLog.d("e:" + e.getMessage());
                        }
                    }
                }
                Method method = applicationInfo.getClass().getMethod("getResourcePath");
                try {
                    method.setAccessible(true);
                    String codePath = (String) method.invoke(applicationInfo);
                    // 1. /system/priv-app
                    // 2. /system/app
                    // 3. /system/delapp
                    // 4. /data/app
                    if (codePath.startsWith("/system")) {
                        is_system_app = true;
//                            if (codePath.startsWith("/system/delapp")) {
//                                is_system_app = false;
//                            }
                    } else if (codePath.startsWith("/data")) {
                        is_system_app = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    HSLog.d("err:" + e.getMessage() + " cause:" + e.getCause());
                }
            } catch (NoSuchMethodException e) {
                HSLog.d("e:" + e.getMessage() + " " + e.getCause());
                e.printStackTrace();
            }
        }
        return is_system_app;
    }

    public static String getTopRunningPackageName() {
        String packageName = HSApplication.getContext().getPackageName();
        try {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                UsageStatsManager mUsageStatsManager = (UsageStatsManager) HSApplication.getContext().getSystemService(Context.USAGE_STATS_SERVICE);
                long time = System.currentTimeMillis();
                List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60 * 60 * 10, time);
                if (stats != null) {
                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                    for (UsageStats usageStats : stats) {
                        mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    }
                    if (!mySortedMap.isEmpty()) {
                        packageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    }
                }
                UsageEvents usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 60 * 60, time);
                if (usageEvents != null) {
                    Event event = new Event();
                    Event event_foregroud = new Event();
                    while (usageEvents.hasNextEvent()) {
                        usageEvents.getNextEvent(event);
                        if (event.getEventType() == Event.MOVE_TO_FOREGROUND) {
                            event_foregroud = event;
                        }
                    }
                    if (!TextUtils.isEmpty(event_foregroud.getPackageName())) {
                        packageName = event_foregroud.getPackageName();
                    }
                }
            } else {
                ActivityManager activityManager = (ActivityManager) HSApplication.getContext().getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningTaskInfo> appTasks = activityManager.getRunningTasks(1);
                if (appTasks != null && appTasks.size() > 0) {
                    ComponentName componentInfo = appTasks.get(0).topActivity;
                    packageName = componentInfo.getPackageName();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        HSLog.d("RELEASE:" + VERSION.RELEASE + " SDK_INT:" + VERSION.SDK_INT + " TopRunningPackage:" + packageName);
        return packageName;
    }

    public static ArrayList<String> getHomePackages() {
        ArrayList<String> homePackages = new ArrayList<String>();
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            homePackages.add(ri.activityInfo.packageName);
        }
        return homePackages;
    }

    public static boolean isInputApp(String packageName) {
        boolean isInput = false;
        try {
            PackageManager packageManager = HSApplication.getContext().getPackageManager();
            PackageInfo pkgInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SERVICES);
            if (pkgInfo != null) {
                ServiceInfo[] serviceInfos = pkgInfo.services;
                if (serviceInfos != null) {
                    for (int i = 0; i < serviceInfos.length; i++) {
                        ServiceInfo serviceInfo = serviceInfos[i];
                        if (serviceInfo.permission != null && serviceInfo.permission.equals("android.permission.BIND_INPUT_METHOD")) {
                            isInput = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return isInput;
    }

    private static boolean isContainInList(String target, List<String> list) {
        boolean isContain = false;
        List<String> listTemp = new ArrayList<>();
        listTemp.addAll(list);
        try {
            for (String item : listTemp) {
                if (target.contains(item)) {
                    isContain = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isContain;
    }

    public static List<BoostApp> getInstalledAppList() {
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        final List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        final List<BoostApp> boostAppList = new ArrayList<>();
        for (ApplicationInfo applicationInfo : packages) {
            BoostApp item = new BoostApp(applicationInfo.packageName);
            if (!isValidApp(item)) {
                continue;
            }
            boostAppList.add(item);
        }
        return boostAppList;
    }

    public static int getRunningAppCountForBattery() {
        return getRunningAppCount();
    }

    public static int getRunningAppCount() {
        int count = 0;
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            count = getRunningAppCountFromService();
        } else {
            count = getRunningAppCountFromProcesses();
        }
        return count;
    }

    private static int getRunningAppCountFromProcesses() {
        int count = 0;
        ActivityManager am = (ActivityManager) HSApplication.getContext().getSystemService(HSApplication.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return count;
        }
        count = runningApps.size();
        return count;
    }

    private static int getRunningAppCountFromService() {
        int count = 0;
        ActivityManager am = (ActivityManager) HSApplication.getContext().getSystemService(HSApplication.ACTIVITY_SERVICE);
        // get running services
        List<ActivityManager.RunningServiceInfo> runningServicesList = am.getRunningServices(Integer.MAX_VALUE);
        if (runningServicesList == null) {
            return count;
        }
        count = runningServicesList.size();
        return count;
    }


    public static List<BoostApp> getRunningAppInfoForMemory() {
        List<BoostApp> runningBoostApp = getRunningAppInfoItem();
        return getFilteredAppsForMemory(runningBoostApp, false);
    }

    public static List<BoostApp> getIgnoreListForMemory() {
        List<BoostApp> installedAppList = getInstalledAppList();
        return getFilteredAppsForMemory(installedAppList, true);
    }

    public static List<BoostApp> getNotIgnoreListForMemory() {
        List<BoostApp> installedAppList = getInstalledAppList();
        return getFilteredAppsForMemory(installedAppList, false);
    }

    private static List<BoostApp> getFilteredAppsForMemory(List<BoostApp> appList, boolean isInWhiteList) {
        List<BoostApp> boostAppList = new ArrayList<>();
        List<String> whiteList = MemoryPrefsManager.getInstance().getWhiteList();
        List<String> blackListByUser = MemoryPrefsManager.getInstance().getBlackListByUser();
        for (BoostApp boostApp : appList) {
            String packageName = boostApp.getPackageName();
            boolean flag = isContainInList(packageName, whiteList)
                    && !isContainInList(packageName, blackListByUser);
            if (isInWhiteList) {
                if (flag) {
                    boostAppList.add(boostApp);
                }
            } else {
                if (!flag) {
                    boostAppList.add(boostApp);
                }
            }
        }
        return boostAppList;
    }

    public static List<BoostApp> getRunningAppInfoItem() {
        List<BoostApp> boostAppList;
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            boostAppList = getRunningAppFromService();
        } else {
            boostAppList = getRunningAppFromProcesses();
        }
        return boostAppList;
    }

    private static List<BoostApp> getRunningAppFromService() {
        ArrayList<BoostApp> result_apps = new ArrayList<>();
        ActivityManager am = (ActivityManager) HSApplication.getContext().getSystemService(HSApplication.ACTIVITY_SERVICE);
        // get running services
        List<ActivityManager.RunningServiceInfo> runningServicesList = am.getRunningServices(Integer.MAX_VALUE);
        if (runningServicesList == null) {
            return result_apps;
        }
        // group by packageName
        Hashtable<String, List<ActivityManager.RunningServiceInfo>> runningServiceInfoTable = new Hashtable<>();
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServicesList) {
            String pkgName = runningServiceInfo.service.getPackageName();
            if (runningServiceInfoTable.get(pkgName) == null) {
                List<ActivityManager.RunningServiceInfo> list = new ArrayList<>();
                list.add(runningServiceInfo);
                runningServiceInfoTable.put(pkgName, list);
            } else {
                runningServiceInfoTable.get(pkgName).add(runningServiceInfo);
            }
        }
        for (Iterator it = runningServiceInfoTable.keySet().iterator(); it.hasNext(); ) {
            String packageName = (String) it.next();
            List<ActivityManager.RunningServiceInfo> runningServiceInfos = runningServiceInfoTable.get(packageName);
            BoostApp item = new BoostApp(packageName, 0);
            if (!isValidApp(item)) {
                continue;
            }
            if (isPkgNameExistedInList(item, result_apps)) {
                continue;
            }
            ArrayList<Integer> pids = new ArrayList<>();
            for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServiceInfos) {
                if (runningServiceInfo.process.equalsIgnoreCase(packageName)) {
                    item.setPid(runningServiceInfo.pid);
                }
                pids.add(runningServiceInfo.pid);
            }
            int[] temp = new int[pids.size()];
            int t = 0;
            for (Integer a1 : pids) {
                temp[t] = a1;
                t++;
            }
            item.setPidArray(temp);
            if (item.getPid() < 0) {
                item.setPid(temp[0]);
            }
            item.setIsChecked(true);
            result_apps.add(item);
            HSLog.d(" getRunningApp:" + item.getApplicationName() + " pkg:" + item.getPackageName() + " isSys:" + isSystemApp(item.getPackageName()));
        }
        List<AndroidAppProcess> processes = ProcessUtils.getRunningAppProcesses(HSApplication.getContext());
        for (AndroidAppProcess androidAppProcess : processes) {
            BoostApp item = new BoostApp(androidAppProcess.getPackageName());
            if (!isValidApp(item)) {
                continue;
            }
            if (isPkgNameExistedInList(item, result_apps)) {
                continue;
            }
            item.setPid(androidAppProcess.pid);
            ArrayList<Integer> pids = new ArrayList<>();
            pids.add(androidAppProcess.pid);
            int[] temp = new int[pids.size()];
            int t = 0;
            for (Integer a1 : pids) {
                temp[t] = a1;
                t++;
            }
            item.setPidArray(temp);
            item.setIsChecked(true);
            result_apps.add(item);
            HSLog.d(" getRunningApp:" + item.getApplicationName() + " pkg:" + item.getPackageName() + " isSys:" + isSystemApp(item.getPackageName()));
        }
        return result_apps;
    }

    private static ArrayList<BoostApp> getRunningAppFromProcesses() {
        ArrayList<BoostApp> result_apps = new ArrayList<>();
        ActivityManager am = (ActivityManager) HSApplication.getContext().getSystemService(HSApplication.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return result_apps;
        }
        // group by packageName
        Hashtable<String, List<ActivityManager.RunningAppProcessInfo>> runningAppProcessInfoTable = new Hashtable<>();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningApps) {
            String pkgName = runningAppProcessInfo.processName;
            if (runningAppProcessInfoTable.get(pkgName) == null) {
                List<ActivityManager.RunningAppProcessInfo> list = new ArrayList<>();
                list.add(runningAppProcessInfo);
                runningAppProcessInfoTable.put(pkgName, list);
            } else {
                runningAppProcessInfoTable.get(pkgName).add(runningAppProcessInfo);
            }
        }
        for (Iterator it = runningAppProcessInfoTable.keySet().iterator(); it.hasNext(); ) {
            String packageName = (String) it.next();
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = runningAppProcessInfoTable.get(packageName);
            BoostApp item = new BoostApp(packageName, 0);
            if (!isValidApp(item)) {
                continue;
            }
            if (isPkgNameExistedInList(item, result_apps)) {
                continue;
            }
            ArrayList<Integer> pids = new ArrayList<>();
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfos) {
                if (runningAppProcessInfo.processName.equalsIgnoreCase(packageName)) {
                    item.setPid(runningAppProcessInfo.pid);
                }
                pids.add(runningAppProcessInfo.pid);
            }
            int[] temp = new int[pids.size()];
            int t = 0;
            for (Integer a1 : pids) {
                temp[t] = a1;
                t++;
            }
            item.setPidArray(temp);
            if (item.getPid() < 0) {
                item.setPid(temp[0]);
            }
            item.setIsChecked(true);
            result_apps.add(item);
            HSLog.d(" getRunningApp:" + item.getApplicationName() + " pkg:" + item.getPackageName() + " isSys:" + isSystemApp(item.getPackageName()));
        }
        return result_apps;
    }

    private static boolean isPkgNameExistedInList(BoostApp
                                                          boostApp, ArrayList<BoostApp> boostAppArrayList) {
        boolean isExisted = false;
        for (BoostApp boostApp1 : boostAppArrayList) {
            if (TextUtils.equals(boostApp.getPackageName(), boostApp1.getPackageName())) {
                isExisted = true;
                break;
            }
        }
        return isExisted;
    }

    private static boolean isValidApp(BoostApp boostApp) {
        if (TextUtils.isEmpty(boostApp.getApplicationName())) {
            // 忽略无 app name 的
            return false;
        }
        if (TextUtils.equals(boostApp.getPackageName(), HSApplication.getContext().getPackageName())) {
            // 忽略自己
            return false;
        }
        if (boostApp.getPackageName().equals("android")) {
            return false;
        }
        return true;
    }
}
package com.ihs.booster.boost.common;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.booster.boost.common.viewdata.BoostApp;
import com.ihs.booster.utils.AppUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sharp on 16/4/5.
 */
public class BaseBlackWhiteListPrefs {
    private static final String PREFS_WHITE_LIST_BY_USER = "PREFS_WHITE_LIST_BY_USER";
    private static final String PREFS_BLACK_LIST_BY_USER = "PREFS_BLACK_LIST_BY_USER";

    private static final String PREFS_CLEANED = "PREFS_CLEANED";

    // 白名单 优先级小于 黑名单
    protected String prefsFileName;
    protected String configNode;

    public BaseBlackWhiteListPrefs(String prefsFileName, String configNode) {
        this.prefsFileName = prefsFileName;
        this.configNode = configNode;
    }

    // noDisplay + hate
    public List<String> getBlackList() {
        List<String> noDisplayList = getDefaultNoDisplayList();
        List<String> blackListByUser = getBlackListByUser();
        noDisplayList.addAll(blackListByUser);
        return removeSameItem(noDisplayList);
    }

    public List<String> getDefaultWhiteList() {
        List<String> defaultList = (ArrayList<String>) HSConfig.getList("Application", configNode, "WhiteAppsList");
        return removeSameItem(defaultList);
    }

    public List<String> getDefaultSystemAppBlackList() {
        List<String> defaultList = (ArrayList<String>) HSConfig.getList("Application", configNode, "SystemAppBlackList");
        return removeSameItem(defaultList);
    }

    public List<String> getDefaultNoDisplayList() {
        List<String> defaultList = (ArrayList<String>) HSConfig.getList("Application", configNode, "NoDisplayAppList");
        return removeSameItem(defaultList);
    }

    public List<String> getWhiteListByUser() {
        String appListString = HSPreferenceHelper.create(HSApplication.getContext(), prefsFileName).getString(PREFS_WHITE_LIST_BY_USER, "");
        if (TextUtils.isEmpty(appListString)) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(Arrays.asList(appListString.split(";")));
        }
    }

    public void addWhiteListByUser(String pkgName) {
        List<String> appList = getWhiteListByUser();
        if (appList.contains(pkgName.toLowerCase())) {
            return;
        }
        appList.add(pkgName.toLowerCase());
        setWhiteListByUser(appList);
        removeBlackListByUser(pkgName);
    }

    public void removeWhiteListByUser(String pkgName) {
        List<String> appList = getWhiteListByUser();
        if (!appList.contains(pkgName.toLowerCase())) {
            return;
        }
        appList.remove(pkgName.toLowerCase());
        setWhiteListByUser(appList);
        addBlackListByUser(pkgName);
    }

    public void setWhiteListByUser(List<String> appList) {
        StringBuilder appBuilder = new StringBuilder();
        for (String pkgName : appList) {
            appBuilder.append(pkgName).append(";");
        }
        HSPreferenceHelper.create(HSApplication.getContext(), prefsFileName).putString(PREFS_WHITE_LIST_BY_USER, appBuilder.toString());
    }

    public List<String> getBlackListByUser() {
        String appListString = HSPreferenceHelper.create(HSApplication.getContext(), prefsFileName).getString(PREFS_BLACK_LIST_BY_USER, "");
        if (TextUtils.isEmpty(appListString)) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(Arrays.asList(appListString.split(";")));
        }
    }

    public void addBlackListByUser(String pkgName) {
        List<String> appList = getBlackListByUser();
        if (appList.contains(pkgName.toLowerCase())) {
            return;
        }
        appList.add(pkgName.toLowerCase());
        setBlackListByUser(appList);
        removeWhiteListByUser(pkgName);
    }

    public void removeBlackListByUser(String pkgName) {
        List<String> appList = getBlackListByUser();
        if (!appList.contains(pkgName.toLowerCase())) {
            return;
        }
        appList.remove(pkgName.toLowerCase());
        setBlackListByUser(appList);
        addWhiteListByUser(pkgName);
    }

    public void setBlackListByUser(List<String> appList) {
        StringBuilder appBuilder = new StringBuilder();
        for (String pkgName : appList) {
            appBuilder.append(pkgName).append(";");
        }
        HSPreferenceHelper.create(HSApplication.getContext(), prefsFileName).putString(PREFS_BLACK_LIST_BY_USER, appBuilder.toString());
    }

    public List<String> getThirdPartyAppList() {
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        final List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        final List<String> appList = new ArrayList<>();
        for (ApplicationInfo applicationInfo : packages) {
            BoostApp item = new BoostApp(applicationInfo.packageName);
            if (!isValidApp(item)) {
                continue;
            }
            if (!AppUtils.isSystemApp(applicationInfo.packageName)) {
                appList.add(item.getPackageName());
            }
        }
        return appList;
    }

    protected boolean isContainInList(String target, List<String> list) {
        boolean isContain = false;
        for (String item : list) {
            if (target.contains(item)) {
                isContain = true;
                break;
            }
        }
        return isContain;
    }

    protected boolean isValidApp(BoostApp boostApp) {
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

    protected List<String> removeSameItem(List<String> list) {
        synchronized (list) {
            for (int i = 0; i < list.size() - 1; i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    if (i < list.size() && j < list.size()) {
                        if (TextUtils.equals(list.get(i).toLowerCase(), list.get(j).toLowerCase())) {
                            list.remove(j);
                            j--;
                        }
                    }
                }
            }
        }
        return list;
    }

    // todo  merge PrefsUtils 里 cpu 相关代码
    public void setCleaned() {
        HSPreferenceHelper.create(HSApplication.getContext(), prefsFileName).putBoolean(PREFS_CLEANED, true);
    }

    public boolean isFirstDoneAfterClean() {
        boolean flag = HSPreferenceHelper.create(HSApplication.getContext(), prefsFileName).getBoolean(PREFS_CLEANED, false);
        if (flag) {
            HSPreferenceHelper.create(HSApplication.getContext(), prefsFileName).putBoolean(PREFS_CLEANED, false);
        }
        return flag;
    }
}

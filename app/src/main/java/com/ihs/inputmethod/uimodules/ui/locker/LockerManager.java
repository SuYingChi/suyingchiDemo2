package com.ihs.inputmethod.uimodules.ui.locker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jixiang on 17/11/17.
 */

public class LockerManager {
    private static final LockerManager ourInstance = new LockerManager();

    public static LockerManager getInstance() {
        return ourInstance;
    }

    private boolean isLockerInstall;
    private List<ILockerInstallStatusChangeListener> lockerInstallStatusChangeListeners;


    private LockerManager() {
    }

    public void init() {
        isLockerInstall = ApkUtils.isInstalled(HSApplication.getContext().getResources().getString(R.string.smart_locker_app_package_name));
        if (!isLockerInstall) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.addDataScheme("package");

            PackageInstallReceiver packageInstallReceiver = new PackageInstallReceiver();
            HSApplication.getContext().registerReceiver(packageInstallReceiver, intentFilter);
        }
    }

    public void addLockerInstallStatusChangeListener(ILockerInstallStatusChangeListener lockerInstallStatusChangeListener) {
        if (lockerInstallStatusChangeListeners == null) {
            lockerInstallStatusChangeListeners = new ArrayList<>();
        }
        lockerInstallStatusChangeListeners.add(lockerInstallStatusChangeListener);
    }

    public void removeLockerInstallStatusChangeListener(ILockerInstallStatusChangeListener lockerInstallStatusChangeListener) {
        if (lockerInstallStatusChangeListeners != null) {
            lockerInstallStatusChangeListeners.remove(lockerInstallStatusChangeListener);
        }
    }

    private void setLockerInstall() {
        isLockerInstall = true;
        if (lockerInstallStatusChangeListeners == null) {
            for (ILockerInstallStatusChangeListener lockerInstallStatusChangeListener : lockerInstallStatusChangeListeners) {
                lockerInstallStatusChangeListener.onLockerInstallStatusChange();
            }
        }
    }

    public boolean isLockerInstall() {
        return isLockerInstall;
    }

    private static class PackageInstallReceiver extends BroadcastReceiver {
        private PackageInstallReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final String packageName = intent.getData().getEncodedSchemeSpecificPart();
            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                if (HSApplication.getContext().getResources().getString(R.string.smart_locker_app_package_name).endsWith(packageName)) {
                    LockerManager.getInstance().setLockerInstall();
                }
            }
        }
    }

    public interface ILockerInstallStatusChangeListener {
        void onLockerInstallStatusChange();
    }
}

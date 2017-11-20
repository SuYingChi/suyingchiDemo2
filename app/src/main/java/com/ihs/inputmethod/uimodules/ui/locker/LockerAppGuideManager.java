package com.ihs.inputmethod.uimodules.ui.locker;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSMarketUtils;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.alerts.HSAlertDialog;
import com.kc.commons.utils.KCCommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jixiang on 17/11/17.
 */

public class LockerAppGuideManager {
    private static final LockerAppGuideManager ourInstance = new LockerAppGuideManager();
    private String lockerAppPkgName;
    private boolean shouldGuideToLockerApp;

    public static LockerAppGuideManager getInstance() {
        return ourInstance;
    }

    private boolean isLockerInstall;
    private List<ILockerInstallStatusChangeListener> lockerInstallStatusChangeListeners;


    private LockerAppGuideManager() {
    }

    public void init(String pkgName,boolean shouldGuideToLockerApp) {
        isLockerInstall = ApkUtils.isInstalled(pkgName);
        if (!isLockerInstall) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            intentFilter.addDataScheme("package");

            PackageInstallReceiver packageInstallReceiver = new PackageInstallReceiver(pkgName);
            HSApplication.getContext().registerReceiver(packageInstallReceiver, intentFilter);

            intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);

            UnlockScreenReceiver unlockScreenReceiver = new UnlockScreenReceiver();
            HSApplication.getContext().registerReceiver(unlockScreenReceiver, intentFilter);
        }
        lockerAppPkgName = pkgName;
        this.shouldGuideToLockerApp = shouldGuideToLockerApp;
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
        if (lockerInstallStatusChangeListeners != null) {
            for (ILockerInstallStatusChangeListener lockerInstallStatusChangeListener : lockerInstallStatusChangeListeners) {
                lockerInstallStatusChangeListener.onLockerInstallStatusChange();
            }
        }
    }

    public boolean isLockerInstall() {
        return isLockerInstall;
    }

    public boolean shouldGuideToDownloadLocker(){
        return !isLockerInstall && shouldGuideToLockerApp;
    }


    public void showDownloadLockerAlert(Context context){
        AlertDialog alertDialog = HSAlertDialog.build(context, R.style.AppCompactDialogStyle).setTitle(context.getString(R.string.locker_guide_unlock_for_free_dialog_title))
                .setPositiveButton(context.getString(R.string.enable), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        browseSmartLocker();
                        KCCommonUtils.dismissDialog((Dialog) dialogInterface);
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        KCCommonUtils.dismissDialog((Dialog) dialogInterface);
                    }
                }).create();
        KCCommonUtils.showDialog(alertDialog);
    }

    public void browseSmartLocker() {
        HSMarketUtils.browseAPP(lockerAppPkgName);
    }

    private static class PackageInstallReceiver extends BroadcastReceiver {
        private String pkgName;
        private PackageInstallReceiver(String pkgName) {
            this.pkgName = pkgName;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final String packageName = intent.getData().getEncodedSchemeSpecificPart();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (pkgName.endsWith(packageName)) {
                    LockerAppGuideManager.getInstance().setLockerInstall();
                }
            }
        }
    }

    private static class UnlockScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LockerAppGuideManager.getInstance().showDownloadLockerAlert(HSApplication.getContext());
        }
    }

    public interface ILockerInstallStatusChangeListener {
        void onLockerInstallStatusChange();
    }
}

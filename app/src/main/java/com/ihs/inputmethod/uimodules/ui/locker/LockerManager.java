package com.ihs.inputmethod.uimodules.ui.locker;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSMarketUtils;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.uimodules.BuildConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.DialogUtils;
import com.ihs.keyboardutils.alerts.HSAlertDialog;

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
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
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

    public boolean shouldGuidToDownloadLocker(){
        return isLockerInstall && BuildConfig.LOCKER_APP_GUIDE;
    }

    public static String getSmartLockerPkName() {
        return HSApplication.getContext().getResources().getString(R.string.smart_locker_app_package_name);
    }

    public void showDownloadLockerAlert(Context context){
        HSAlertDialog.build(context,R.style.AppCompactDialogStyle).setTitle(context.getString(R.string.locker_guide_unlock_for_free_dialog_title))
                .setPositiveButton(context.getString(R.string.enable), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        HSMarketUtils.browseAPP(getSmartLockerPkName());
                        DialogUtils.safeDismissDialog((Dialog) dialogInterface,context);
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DialogUtils.safeDismissDialog((Dialog) dialogInterface,context);
                    }
                }).create().show();
    }

    private static class PackageInstallReceiver extends BroadcastReceiver {
        private PackageInstallReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final String packageName = intent.getData().getEncodedSchemeSpecificPart();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
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

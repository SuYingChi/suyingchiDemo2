package com.ihs.booster.boost.common.viewdata;

import android.graphics.drawable.Drawable;

import com.ihs.boost.HSBoostApp;
import com.ihs.boost.HSBoostFile;
import com.ihs.booster.utils.AppUtils;
import com.ihs.booster.common.expandablelist.data.BaseItemData;

public class BoostApp extends BaseItemData {
    private long mDataSize = 0;
    private int pid = -1;
    private int[] pidArray;
    private String mPackageName;
    private String applicationName;
    private int type = 0;
    private double app_use_energy_percent_value = 0;
    private float powerConsumeScoreInRunningApps = 0;
    private float powerConsumeScoreInAllApps = 0;
    private float cpuOccpyPercentInRunningApps = 0;
    private boolean isAdviceToKeep = false;
    public HSBoostFile hsBoostFile;
    private Drawable appIcon;

    public BoostApp(HSBoostApp hsBoostApp) {
        this.mPackageName = hsBoostApp.getPackageName();
        this.pid = hsBoostApp.getMainPid();
        this.pidArray = hsBoostApp.getPidArray();
        this.mDataSize = hsBoostApp.getSize();
    }

    public BoostApp(HSBoostFile hsBoostFile) {
        this.hsBoostFile = hsBoostFile;
        if (hsBoostFile.isValidApk) {
            this.mPackageName = hsBoostFile.appFilePackageName;
            this.applicationName = hsBoostFile.getApplicationLabel().trim().replace(" ", "");
            this.appIcon = hsBoostFile.appIcon;
        }
        this.mDataSize = hsBoostFile.getSize();
    }

    public BoostApp(String packageName) {
        mDataSize = 0;
        if (packageName.indexOf(":") > 0) {
            packageName = packageName.substring(0, packageName.indexOf(":"));
        }
        mPackageName = packageName;
    }

    public BoostApp(String packageName, long dataSize) {
        this(packageName);
        mDataSize = dataSize;
    }

    public BoostApp(String packageName, long dataSize, int pid) {
        this(packageName, dataSize);
        this.pid = pid;
    }



    public void setCpuOccpyPercentInRunningApps(float cpuOccpyPercentInRunningApps) {
        this.cpuOccpyPercentInRunningApps = cpuOccpyPercentInRunningApps;
    }

    public float getCpuOccupyPercentInRunningApps() {
        return cpuOccpyPercentInRunningApps;
    }

    public void setPowerConsumeScoreInRunningApps(float powerConsumeScoreInRunningApps) {
        this.powerConsumeScoreInRunningApps = powerConsumeScoreInRunningApps;
    }

    public float getPowerConsumeScoreInRunningApps() {
        return powerConsumeScoreInRunningApps;
    }

    public void setPowerConsumeScoreInAllApps(float powerConsumeScoreInAllApps) {
        this.powerConsumeScoreInAllApps = powerConsumeScoreInAllApps;
    }

    public float getPowerConsumeScoreInAllApps() {
        return powerConsumeScoreInAllApps;
    }

    public void setAdviceToKeep(boolean isShowAdvice) {
        this.isAdviceToKeep = isShowAdvice;
    }

    public boolean isAdviceToKeep() {
        return isAdviceToKeep;
    }

    public int[] getPidArray() {
        return pidArray;
    }

    public void setPidArray(int[] pidArray) {
        this.pidArray = pidArray;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getAppUseEnergyPercentValue() {
        return app_use_energy_percent_value;
    }

    public void setAppUseEnergyPercentValue(double app_use_energy_value) {
        this.app_use_energy_percent_value = app_use_energy_value;
    }


    public void setDataSize(long mDataSize) {
        this.mDataSize = mDataSize;
    }

    public Drawable getApplicationIcon() {
        if (appIcon == null) {
            if (hsBoostFile != null) {
                if (hsBoostFile.isValidApk) {
                    appIcon = AppUtils.getApkIcon(hsBoostFile.filePath);
                }
            } else {
                appIcon = AppUtils.getAppIcon(mPackageName);
            }
        }
        return appIcon;
    }

    public String getApplicationName() {
        if (null == applicationName) {
            applicationName = AppUtils.getAppName(mPackageName);
        }
        return applicationName;
    }

    public void setApplicationName(String mApplicationName) {
        this.applicationName = mApplicationName;
    }

    public long getSize() {
        return mDataSize;
    }

    public String getPackageName() {
        return mPackageName;
    }
}

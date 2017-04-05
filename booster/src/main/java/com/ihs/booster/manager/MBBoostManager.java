package com.ihs.booster.manager;

import com.ihs.boost.HSBoostManager;
import com.ihs.booster.boost.common.viewdata.BoostApp;
import com.ihs.booster.common.MBThread;
import com.ihs.booster.common.asynctask.MBAsyncTask.OnProcessListener;
import com.ihs.booster.common.event.MBObserverCenter;
import com.ihs.booster.constants.MBConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sharp on 15/8/19.
 */
public abstract class MBBoostManager {
    public static final MBObserverCenter globalObserverCenter = new MBObserverCenter();
    private static MBThread workThread;

    public static void runOnWorkThread(Runnable action) {
        if (workThread == null) {
            workThread = new MBThread("app_work_thread");
        }
        workThread.run(action);
    }

    protected List<BoostApp> scanAppList = new ArrayList<>();
    protected long scannedTime = 0;

    protected List<BoostApp> cleanAppList = new ArrayList<>();
    protected HSBoostManager hsBoostManager = HSBoostManager.getInstance();
    protected OnProcessListener processListener;

    public abstract boolean isCleanExpired();

    public abstract void scan(OnProcessListener onProcessListener);

    public abstract void clean(OnProcessListener onProcessListener);

    public abstract void cancelScan();

    public abstract void cancelClean();

    public void setScannedTime() {
        this.scannedTime = System.currentTimeMillis();
    }

    public boolean isScanExpired() {
        return System.currentTimeMillis() - scannedTime > MBConfig.SCAN_EXPIRED_SECOND * 1000;
    }

    public List<BoostApp> getCheckedScanAppList() {
        List<BoostApp> cleanApps = new ArrayList<>();
        for (BoostApp item : scanAppList) {
            if (item.isChecked()) {
                cleanApps.add(item);
            }
        }
        return cleanApps;
    }

    public long getCheckedScannedSize() {
        long size = 0;
        for (BoostApp item : scanAppList) {
            if (item.isChecked()) {
                size += item.getSize();
            }
        }
        return size;
    }

    public List<BoostApp> getScanAppList() {
        return scanAppList;
    }

    public void setScanAppList(List<BoostApp> scanAppList) {
        this.scanAppList = scanAppList;
    }

    public List<BoostApp> getCleanAppList() {
        return cleanAppList;
    }

    public void cancel() {
        cancelClean();
        cancelScan();
        processListener = null;
    }

    public void reset() {
        scannedTime = 0;
        cancel();
    }
}
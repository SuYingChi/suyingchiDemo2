package com.ihs.booster.manager;

import com.ihs.boost.HSBoostApp;
import com.ihs.boost.HSBoostManager.BoostListener;
import com.ihs.booster.boost.common.PrefsUtils;
import com.ihs.booster.boost.common.viewdata.BoostApp;
import com.ihs.booster.boost.floating.FloatPrefManager;
import com.ihs.booster.boost.memory.MemoryPrefsManager;
import com.ihs.booster.common.asynctask.BoostProgress;
import com.ihs.booster.common.asynctask.MBAsyncTask.OnProcessListener;
import com.ihs.booster.constants.MBConfig;
import com.ihs.booster.utils.L;
import com.ihs.booster.utils.Utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by sharp on 15/8/19.
 */
public class MBMemoryManager extends MBBoostManager {

    private static MBMemoryManager instance;
    private long totalMemorySize = 0;
    private long availableMemorySize = 0;
    private long memoryScannedDataSize;
    private long memoryDataCleanedSize;
    private float lastMemoryCleanedPercent = 1;


    public synchronized static MBMemoryManager getInstance() {
        if (instance == null) {
            instance = new MBMemoryManager();
        }
        return instance;
    }

    private MBMemoryManager() {
        MBBoostManager.runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                memoryDataCleanedSize = PrefsUtils.getLastMemoryBoostedSize();
                lastMemoryCleanedPercent = PrefsUtils.getLastMemoryBoostedPercent();
                totalMemorySize = Utils.getMemoryTotalSize();
                availableMemorySize = Utils.getMemoryAvailableSize();
            }
        });
//        HSApplication.globalObserverCenter.addObserver(this, MBObserverEvent.IGNORE_APP_LIST_ADD, new Observer() {
//            @Override
//            public void update(Observable observable, Object data) {
//                String packageName = (String) data;
//                if (TextUtils.isEmpty(packageName)) {
//                    return;
//                }
//                for (int i = 0; i < scanAppList.size(); i++) {
//                    if (scanAppList.get(i).getPackageName().equalsIgnoreCase(packageName)) {
//                        scanAppList.remove(i);
//                        break;
//                    }
//                }
//            }
//        });
    }

    @Override
    public void scan(OnProcessListener onProcessListener) {
        if (this.processListener != null) {
            return;
        }
        this.processListener = onProcessListener;
        scanAppList.clear();
        memoryScannedDataSize = 0;
        if (this.processListener != null) {
            this.processListener.onStarted();
        }
        float memPercent = MBMemoryManager.getInstance().getRealTimeUsedPercent();
        FloatPrefManager.setMemPercentBeforeClean(memPercent);
        hsBoostManager.memoryScan.addListener(new BoostListener<HSBoostApp>() {
            @Override
            public void onProgressed(int processedCount, int total, HSBoostApp hsBoostApp) {
                BoostApp boostApp = new BoostApp(hsBoostApp);
                if (boostApp.getSize() > 0) {
                    memoryScannedDataSize += boostApp.getSize();
                    scanAppList.add(boostApp);
                    PrefsUtils.setLastMemoryCleanedTime();
                    PrefsUtils.setLastMemoryBoostedSize(memoryDataCleanedSize);
                    if (processListener != null) {
                        processListener.onProgressUpdated(new BoostProgress(scanAppList.size(), total, memoryScannedDataSize, boostApp));
                    }
                }
            }

            @Override
            public void onCompleted(List<HSBoostApp> apps, long dataSize) {
                Collections.sort(scanAppList, new Comparator<BoostApp>() {
                    @Override
                    public int compare(BoostApp lhs, BoostApp rhs) {
                        return (int) (rhs.getSize() - lhs.getSize());
                    }
                });
                setScannedTime();
                if (processListener != null) {
                    processListener.onCompleted(scanAppList);
                    processListener = null;
                }

            }
        }, null);
        hsBoostManager.memoryScan.start(MemoryPrefsManager.getInstance().getWhiteList(), MemoryPrefsManager.getInstance().getBlackList());
    }

    @Override
    public void clean(OnProcessListener onProcessListener) {
        cleanAppList.clear();
        MBConfig.changeMemoryExpiredTime();
        memoryDataCleanedSize = getCheckedScannedSize();
        L.l("memoryDataCleanedSize:" + memoryDataCleanedSize);
        lastMemoryCleanedPercent = memoryDataCleanedSize * 100 / totalMemorySize;
        PrefsUtils.setLastMemoryCleanedTime();
        PrefsUtils.setLastMemoryBoostedPercent(lastMemoryCleanedPercent);
        PrefsUtils.setLastMemoryBoostedSize(memoryDataCleanedSize);
        hsBoostManager.memoryClean.addListener(new BoostListener<HSBoostApp>() {
            @Override
            public void onProgressed(int processedCount, int total, HSBoostApp hsBoostApp) {
//                BoostApp boostApp = new BoostApp(hsBoostApp);
//                memoryCleanApps.add(boostApp);
            }

            @Override
            public void onCompleted(List<HSBoostApp> apps, long dataSize) {
                for (int i = 0; i < scanAppList.size(); i++) {
                    if (scanAppList.get(i).isChecked()) {
                        scanAppList.remove(i);
                        i--;
                    }
                }
                L.l("memoryCleanTask:onCompleted:" + memoryDataCleanedSize);
//                HSApplication.globalObserverCenter.notifyOnUIThread(MBObserverEvent.CLEAN_MEMORY_FINISHED, apps);
            }
        }, null);
        hsBoostManager.memoryClean.start();
        L.l("hsBoostManager.memoryClean.start()");
    }

    @Override
    public void cancelClean() {
        hsBoostManager.memoryClean.cancelMultiProcess();
    }

    @Override
    public void cancelScan() {
        hsBoostManager.memoryScan.cancelMultiProcess();
    }

    @Override
    public boolean isCleanExpired() {
        return PrefsUtils.isLastMemoryCleanedExpired();
    }

    public long getTotalMemorySize() {
        return totalMemorySize;
    }

    public long getAvailableMemorySize() {
        return availableMemorySize;
    }

    public void setAvailableMemorySize(long availableMemorySize) {
        this.availableMemorySize = availableMemorySize;
    }

    public float getUsedPercent() {
        if (totalMemorySize == 0) {
            totalMemorySize = Utils.getMemoryTotalSize();
        }
        return 100 - availableMemorySize * 100 / totalMemorySize;
    }

    public float getRealTimeUsedPercent() {
        availableMemorySize = Utils.getMemoryAvailableSize();
        return getUsedPercent();
    }

    public float getUsedPercentFake() {
        if (PrefsUtils.isLastMemoryCleanedExpired() || FloatPrefManager.getLastBoostedMemUsage() == 0) {
            return MBMemoryManager.getInstance().getRealTimeUsedPercent();
        } else {
            return FloatPrefManager.getLastBoostedMemUsage();
        }
    }

    //用于计算系统分数
    public int getMemoryScore() {
        if (isCleanExpired()) {
            return (int) (100 - getUsedPercent());
        } else {
            return 100;
        }
    }
}
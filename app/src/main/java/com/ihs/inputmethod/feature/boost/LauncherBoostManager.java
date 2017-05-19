package com.ihs.inputmethod.feature.boost;

import com.ihs.device.clean.accessibility.HSAccTaskManager;
import com.ihs.device.clean.memory.HSAppMemoryManager;

import java.util.ArrayList;
import java.util.List;

public abstract class LauncherBoostManager {

    public interface OnProcessListener {
        void onStart();

        void onProgress(BoostProgress boostProgress, long totalDataSize);

        void onComplete(List<BoostApp> apps);
    }

    protected List<BoostApp> mScanAppList = new ArrayList<>();
    protected long mScannedTimeMillis;

    protected List<BoostApp> mCleanAppList = new ArrayList<>();

    protected HSAppMemoryManager mMemoryManager = HSAppMemoryManager.getInstance();
    protected HSAccTaskManager mAccessibilityManager = HSAccTaskManager.getInstance();
    protected OnProcessListener mProcessListener;

    public abstract boolean isEnable();

    public abstract boolean isCleanExpired();

    public abstract void scan(OnProcessListener onProcessListener);

    public abstract void clean(HSAccTaskManager.AccTaskListener listener);

    public abstract void cancelScan();

    public abstract void cancelClean();

    public void setScannedTime() {
        mScannedTimeMillis = System.currentTimeMillis();
    }

    public List<BoostApp> getScanAppList() {
        return mScanAppList;
    }

    public void cancel() {
        cancelClean();
        cancelScan();
        mProcessListener = null;
    }

    public void reset() {
        mScannedTimeMillis = 0;
        cancel();
    }

    public static class BoostProgress {

        public int processedCount;
        public int total;

        public BoostProgress(int processedCount, int total) {
            this.processedCount = processedCount;
            this.total = total;
        }
    }
}

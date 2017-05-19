package com.ihs.inputmethod.feature.boost;

import android.os.Handler;

import com.honeycomb.launcher.util.ConcurrentUtils;
import com.honeycomb.launcher.util.PreferenceHelper;
import com.honeycomb.launcher.util.Utils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.device.clean.accessibility.HSAccTaskManager;
import com.ihs.device.clean.memory.HSAppMemory;
import com.ihs.device.clean.memory.HSAppMemoryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class LauncherMemoryManager extends LauncherBoostManager {

    private static final String PREF_KEY_IGNORE_ADVANCED_BOOST_TIME = "ignore_advanced_boost_time";

    private static LauncherMemoryManager sInstance;

    private long mTotalMemorySize;
    private long mAvailableMemorySize;
    private long mMemoryScannedDataSize;

    private Handler mHandler = new Handler();

    public synchronized static LauncherMemoryManager getInstance() {
        if (sInstance == null) {
            sInstance = new LauncherMemoryManager();
        }
        return sInstance;
    }

    private LauncherMemoryManager() {
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                mTotalMemorySize = Utils.getMemoryTotalSize();
                mAvailableMemorySize = Utils.getMemoryAvailableSize();
            }
        });
    }

    // TODO: 不能用单 listener 做，改为 add listener
    @Override
    public void scan(OnProcessListener onProcessListener) {
        if (this.mProcessListener != null) {
            return;
        }
        this.mProcessListener = onProcessListener;
        mScanAppList.clear();
        mMemoryScannedDataSize = 0;
        if (this.mProcessListener != null) {
            this.mProcessListener.onStart();
        }
        @SuppressWarnings("unchecked")
        List<String> whiteList = (List<String>) HSConfig.getList("Application", "WhiteAppsList");
        mMemoryManager.setGlobalScanExcludeList(whiteList);
        mMemoryManager.startScanWithCompletedProgress(new HSAppMemoryManager.MemoryTaskListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgressUpdated(int processedCount, int total, HSAppMemory hsAppMemory) {
                if (hsAppMemory.getSize() > 0) {
                    mMemoryScannedDataSize += hsAppMemory.getSize();
                    mScanAppList.add(new BoostApp(hsAppMemory));
                    if (mProcessListener != null) {
                        mProcessListener.onProgress(new BoostProgress(processedCount, total), mMemoryScannedDataSize);
                    }
                }
            }

            @Override
            public void onSucceeded(List<HSAppMemory> list, long l) {
                Collections.sort(mScanAppList, new Comparator<BoostApp>() {
                    @Override
                    public int compare(BoostApp lhs, BoostApp rhs) {
                        return (int) (rhs.getSize() - lhs.getSize());
                    }
                });
                setScannedTime();
                if (mProcessListener != null) {
                    mProcessListener.onComplete(mScanAppList);
                    mProcessListener = null;
                }
            }

            @Override
            public void onFailed(int i, String s) {
            }
        });
    }

    public long getScannedSize() {
        return mMemoryScannedDataSize;
    }

    private List<String> getCleanPackages() {
        List<String> pkgs = new ArrayList<>(mCleanAppList.size());
        for (BoostApp item : mScanAppList) {
            HSLog.d("Boost.Access", "pkg to be clean : " + item.getPackageName());
            pkgs.add(item.getPackageName());
        }
        return pkgs;
    }

    @Override
    public void clean(final HSAccTaskManager.AccTaskListener listener) {
        try {
            List<String> cleanPackages = getCleanPackages();
            if (cleanPackages != null && cleanPackages.size() > 0) {
                mAccessibilityManager.startForceStop(cleanPackages,listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancelClean() {
        mAccessibilityManager.cancel();
    }

    @Override
    public void cancelScan() {
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public boolean isCleanExpired() {
        return true;
    }

    public long getTotalMemorySize() {
        if (mTotalMemorySize != 0) {
            return mTotalMemorySize;
        } else {
            return Utils.getMemoryTotalSize();
        }
    }

    public long getAvailableMemorySize() {
        return mAvailableMemorySize;
    }

    public void setAvailableMemorySize(long availableMemorySize) {
        mAvailableMemorySize = availableMemorySize;
    }

    public float getUsedPercent() {
        if (mTotalMemorySize == 0) {
            mTotalMemorySize = Utils.getMemoryTotalSize();
        }
        return 100 - mAvailableMemorySize * 100 / mTotalMemorySize;
    }

    public float getRealTimeUsedPercent() {
        mAvailableMemorySize = Utils.getMemoryAvailableSize();
        return getUsedPercent();
    }

    // 用于计算系统分数
    public int getMemoryScore() {
        if (isCleanExpired()) {
            return (int) (100 - getUsedPercent());
        } else {
            return 100;
        }
    }

    // 内存清理后，在内存清理失效之前扫描
    public void startScanTimer() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LauncherMemoryManager.getInstance().scan(null);
            }
        }, (120 - 8) * 1000);
    }

    public void ignoreAdvancedBoost() {
        PreferenceHelper.getDefault().incrementAndGetInt(PREF_KEY_IGNORE_ADVANCED_BOOST_TIME);
    }

    public void clearIgnoreBoostTime() {
        HSPreferenceHelper.getDefault().putInt(PREF_KEY_IGNORE_ADVANCED_BOOST_TIME, 0);
    }

    public boolean shouldShowAdvancedBoostTip() {
        int ignoreTime = HSPreferenceHelper.getDefault().getInt(PREF_KEY_IGNORE_ADVANCED_BOOST_TIME, 0);
        return ignoreTime <= 5;
    }
}

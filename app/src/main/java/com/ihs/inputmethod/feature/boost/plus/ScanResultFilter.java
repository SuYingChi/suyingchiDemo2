package com.ihs.inputmethod.feature.boost.plus;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.memory.HSAppMemory;

import java.util.List;
import java.util.Map;

public class ScanResultFilter {

    private static final String TAG = ScanResultFilter.class.getSimpleName();

    private static final long CLEAN_EFFECTIVE_TIME = 2 * 60 * 1000; // 2 minute
    private static final long RUNNING_TIME_EXCLUDE_THRESHOLD = 2 * 60 * 1000; // 2 minutes

    private List<String> mSystemKillList;

    private static BoostPlusActivity.AppsSelection sLastSelection;
    private static long sLastCleanTime;
    private Map<String, String> mSelectedAppsMap;

    public ScanResultFilter() {
        //noinspection unchecked
        mSystemKillList = (List<String>) HSConfig.getList("Application", "BoostPlus", "SystemAppsKillList");
    }

    void initSelectedAppsMap() {
        mSelectedAppsMap = BoostPlusUtils.getCleanedAppsMap();
    }

    void setLastSelection(BoostPlusActivity.AppsSelection lastSelection) {
        // No copy here, lists shall be maintained outside
        sLastSelection = lastSelection;
    }

    void setLastCleanTime(long lastCleanTimeMillis) {
        sLastCleanTime = lastCleanTimeMillis;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean filter(Context context, HSAppMemory appMemory) {
        String packageName = appMemory.getPackageName();

        // Time-independent
        if (appMemory.getSize() == 0) {
            HSLog.d(TAG, "[✘] " + packageName + " || App with zero memory size");
            return false;
        }
        if (appMemory.isSysApp() && !mSystemKillList.contains(packageName)) {
            HSLog.d(TAG, "[✘] " + packageName + " || System app not explicitly configured to include");
            return false;
        }
        if (context.getPackageName().equals(packageName)) {
            HSLog.d(TAG, "[✘] " + packageName + " || Myself");
            return false;
        }
        if (appMemory.getAppName().equals(packageName)) {
            HSLog.d(TAG, "[✘] " + packageName + " || App name is package name");
            return false;
        }

        if (null != mSelectedAppsMap && mSelectedAppsMap.containsKey(packageName)) {
            long cleanTime = 0;
            try {
                String cleanTimeText = mSelectedAppsMap.get(packageName);
                cleanTime = Long.parseLong(cleanTimeText);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            if (0 != cleanTime && System.currentTimeMillis() - cleanTime < RUNNING_TIME_EXCLUDE_THRESHOLD) {
                HSLog.d(TAG, "[✘] " + packageName + " || App not shown in scan result for cleaned time < 2 min");
                return false;
            } else {
                BoostPlusUtils.removeCleanedAppsMap(packageName);
            }
        }

        long now = SystemClock.elapsedRealtime();
        if (now - sLastCleanTime > CLEAN_EFFECTIVE_TIME) {
            HSLog.d(TAG, "[✓] " + packageName + " || No recent clean, so no need to against check last selection");
            return true;
        }

        // Time-dependent
        if (sLastSelection != null) {
            for (HSAppMemory excludedApp : sLastSelection.unselectedApps) {
                if (TextUtils.equals(excludedApp.getPackageName(), packageName)) {
                    HSLog.d(TAG, "[✓] " + packageName + " || App NOT selected in previous clean");
                    return true;
                }
            }
            for (HSAppMemory excludedApp : sLastSelection.selectedApps) {
                if (TextUtils.equals(excludedApp.getPackageName(), packageName)) {
                    HSLog.d(TAG, "[✘] " + packageName + " || App selected in previous clean");
                    return false;
                }
            }
        }
        if (SystemClock.elapsedRealtime() - appMemory.getMainProcessLaunchTime() < RUNNING_TIME_EXCLUDE_THRESHOLD) {
            HSLog.d(TAG, "[✘] " + packageName + " || App not shown in scan result for previous clean, running time < 2 min");
            return false;
        }

        HSLog.d(TAG, "[✓] " + packageName);
        return true;
    }
}

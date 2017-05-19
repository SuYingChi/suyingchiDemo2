package com.ihs.inputmethod.feature.boost;

import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.honeycomb.launcher.BuildConfig;
import com.honeycomb.launcher.dialog.LauncherTipManager;
import com.honeycomb.launcher.model.LauncherFiles;
import com.honeycomb.launcher.util.ConcurrentUtils;
import com.honeycomb.launcher.util.PreferenceHelper;
import com.honeycomb.launcher.util.Utils;
import com.honeycomb.launcher.wizard.SetAsDefaultBoostedTipInfo;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.util.ArrayList;
import java.util.List;

public class BoostConditionManager {

    private static final String TAG = "BoostNotification";

    public static final int EFFECTIVE_BOOST_PERCENTAGE_THRESHOLD = 1;

    private static final BoostType DEBUG_BOOST_TYPE = BuildConfig.DEBUG ? null : null;
    private static final int DEBUG_CRITICAL_BATTERY_LEVEL = BuildConfig.DEBUG ? 80 : 20;

    private static final long BOOST_NOTIFICATION_MIN_INTERVAL = BuildConfig.DEBUG ?
            (5 * DateUtils.MINUTE_IN_MILLIS) : (6 * DateUtils.HOUR_IN_MILLIS);
    private static final int CONDITION_FULFILL_DURATION_MINUTES = 5;

    public static final String PREF_KEY_LAST_BOOST_NOTIFICATION_TIME = "last_boost_notification_time";

    public interface ConditionChangeListener {
        void onBoostNeeded(BoostType type);

        void onBoostNotNeeded(BoostType previousType);
    }

    private List<ConditionChangeListener> mListeners = new ArrayList<>(2);

    private Integer mLowBatteryCount = 5;
    private Integer mHotCpuCount = 0;
    private Integer mLowRamCount = 0;

    /**
     * Type of boost needed. {@code null} when boost is not needed.
     */
    private BoostType mCurrentBoostType;

    private BoostType mPendingHighlightType;

    private volatile static BoostConditionManager sInstance;

    public static BoostConditionManager getInstance() {
        if (sInstance == null) {
            synchronized (BoostConditionManager.class) {
                if (sInstance == null) {
                    sInstance = new BoostConditionManager();
                }
            }
        }
        return sInstance;
    }

    private BoostConditionManager() {
    }

    public synchronized void addConditionChangeListener(ConditionChangeListener listener) {
        mListeners.add(listener);
    }

    public synchronized void removeConditionChangeListener(ConditionChangeListener listener) {
        mListeners.remove(listener);
    }

    public @NonNull
    BoostType getCurrentBoostType() {
        if (mCurrentBoostType == null) {
            // Defaults to RAM when non of the RAM / BATTERY / CPU_TEMPERATURE condition is satisfied
            return BoostType.RAM;
        }
        return mCurrentBoostType;
    }

    public BoostType pollPendingHighlightType() {
        BoostType pendingType = mPendingHighlightType;
        mPendingHighlightType = null;
        return pendingType;
    }

    public void reportMinuteData(int batteryLevel, boolean isCharging, float cpuTemperature, int ramUsage) {
        HSLog.d(TAG, "Time tick, battery " + batteryLevel + "%, CPU temp " + cpuTemperature
                + ", RAM " + ramUsage + "%, isCharging " + isCharging);
        if (DEBUG_BOOST_TYPE != null) {
            setCurrentBoostType(DEBUG_BOOST_TYPE);
            notify(DEBUG_BOOST_TYPE, true);
            return;
        }

        // CAUTION: Positive trigger after count 5, Negative trigger immediately.
        // Battery
        if (batteryLevel <= DEBUG_CRITICAL_BATTERY_LEVEL && !isCharging) {
            mLowBatteryCount++;
            if (mLowBatteryCount >= CONDITION_FULFILL_DURATION_MINUTES) {
                mLowBatteryCount = 0;
                if (mCurrentBoostType == null) {
                    notify(BoostType.BATTERY, true);
                }
            }
        } else {
            mLowBatteryCount = 0;
            if (mCurrentBoostType == BoostType.BATTERY) {
                notify(BoostType.BATTERY, false);
            }
        }

        // CPU
        if (cpuTemperature >= 45) {
            mHotCpuCount++;
            if (mHotCpuCount >= CONDITION_FULFILL_DURATION_MINUTES) {
                mHotCpuCount = 0;
                if (mCurrentBoostType == null) {
                    notify(BoostType.CPU_TEMPERATURE, true);
                }
            }
        } else {
            mHotCpuCount = 0;
            if (mCurrentBoostType == BoostType.CPU_TEMPERATURE) {
                notify(BoostType.CPU_TEMPERATURE, false);
            }
        }

        // RAM
        if (ramUsage >= 80) {
            mLowRamCount++;
            mLowRamCount = 0;
            if (mCurrentBoostType == null) {
                notify(BoostType.RAM, true);
            }
        } else {
            mLowRamCount = 0;
            if (mCurrentBoostType == BoostType.RAM) {
                notify(BoostType.RAM, false);
            }
        }
    }

    public void reportBoostDone(int percentageBoosted) {
        if (percentageBoosted >= EFFECTIVE_BOOST_PERCENTAGE_THRESHOLD) {
            PreferenceHelper.get(LauncherFiles.DESKTOP_PREFS)
                    .incrementAndGetInt(SetAsDefaultBoostedTipInfo.PREF_KEY_EFFECTIVE_BOOST_TIMES);
        }
        if (mCurrentBoostType != null) {
            BoostType type = mCurrentBoostType;
            setCurrentBoostType(null);
            notify(type, false);
        }
    }

    private void setCurrentBoostType(BoostType type) {
        mCurrentBoostType = type;
        mPendingHighlightType = type;
    }

    private synchronized void notify(final BoostType type, boolean deviceNeedBoost) {
        // Not always notify user, DND!
        if (deviceNeedBoost
                && (hasRemindUserShortlyBefore() || Utils.inSleepTime())) {
            return;
        }
        setCurrentBoostType(deviceNeedBoost ? type : null);

        LauncherTipManager.getInstance().showTip(HSApplication.getContext(),
                LauncherTipManager.TipType.NEED_BOOST_TIP, type, deviceNeedBoost);
    }

    public synchronized void notifyNeedBoost(final BoostType type, boolean isNeeded) {
        for (ConditionChangeListener listener : mListeners) {
            final ConditionChangeListener listenerFinal = listener;
            if (isNeeded) {
                HSLog.d(TAG, "Boost condition " + type + " fulfilled");
                HSPreferenceHelper.getDefault().putLong(PREF_KEY_LAST_BOOST_NOTIFICATION_TIME, System.currentTimeMillis());
                ConcurrentUtils.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        listenerFinal.onBoostNeeded(type);
                    }
                });
            } else {
                HSLog.d(TAG, "Boost condition " + type + " off");
                ConcurrentUtils.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        listenerFinal.onBoostNotNeeded(type);
                    }
                });
            }
        }
    }

    public static boolean hasRemindUserShortlyBefore() {
        long lastNotificationTime = HSPreferenceHelper.getDefault().getLong(PREF_KEY_LAST_BOOST_NOTIFICATION_TIME, -1);
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastNotificationTime;
        HSLog.d(TAG, timeDifference + " ms since last shown boost notification");
        if (timeDifference < BOOST_NOTIFICATION_MIN_INTERVAL) {
            return true;
        }
       return false;
    }
}

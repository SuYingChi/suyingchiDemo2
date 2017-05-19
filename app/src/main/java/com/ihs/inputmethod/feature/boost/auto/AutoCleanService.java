package com.ihs.inputmethod.feature.boost.auto;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.honeycomb.launcher.BuildConfig;
import com.honeycomb.launcher.R;
import com.honeycomb.launcher.boost.plus.BoostPlusSettingsActivity;
import com.honeycomb.launcher.boost.plus.RootHelper;
import com.honeycomb.launcher.receiver.AdminReceiver;
import com.honeycomb.launcher.util.CommonUtils;
import com.honeycomb.launcher.util.PermissionUtils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.device.clean.accessibility.HSAccTaskManager;
import com.ihs.device.clean.memory.HSAppMemory;
import com.ihs.device.clean.memory.HSAppMemoryManager;

import java.util.ArrayList;
import java.util.List;

import static com.honeycomb.launcher.boost.auto.AutoCleanService.Status.COVER_TO_SHOW;
import static com.honeycomb.launcher.boost.auto.AutoCleanService.Status.IDLE;
import static com.honeycomb.launcher.boost.auto.AutoCleanService.Status.NORMAL_CLEAN;
import static com.honeycomb.launcher.boost.auto.AutoCleanService.Status.ROOT_CLEAN;
import static com.honeycomb.launcher.boost.auto.AutoCleanService.Status.SCANNING;
import static com.honeycomb.launcher.boost.auto.AutoCleanService.Status.STOPPING;

/**
 * Service should always alive, we listen for screen off and execute force stop if needed.
 */
public class AutoCleanService extends Service {

    private static final long WAIT_COVER_TIMEOUT = 3000; // 3s
    public static final String TAG = "AutoCleanService";
    private static final long CLEAN_INTERVAL_MIN_TIME_NANO = 30000000000L; //30s
    private static final long CLEAN_SCHEDULE_TIME_MILLS = AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15; // 3m
    private static final String ACTION_CLEAN_NOW = "auto_clean_now";
    private static final String ACTION_STOP_NOW = "auto_clean_stop_now";
    private static final String ACTION_RESTART = "auto_clean_restart";

    public static AutoCleanService sCleanService = null;
    private Handler mHandler = new Handler();
    private LockHelper mLockHelper;
    private TelephonyManager mTelephoneManager;
    private AlarmManager mAlarmManager;
    private PendingIntent mSelfBootIntent;
    private PendingIntent mCleanIntent;

    private List<String> mThirdPartyAllowList;
    private List<String> mSystemAppsNeedClean;
    private long mLastCleanTimeNano;
    private long mLastScreenOffTimeNano;
    private int mScreenOffCount;
    private String mInnerpermission;
    private boolean isCleanSchedule;
    private boolean mInterrupted;
    private boolean mRootMode;

    protected enum Status {
        IDLE,
        COVER_TO_SHOW,
        SCANNING,
        NORMAL_CLEAN,
        ROOT_CLEAN,
        STOPPING
    }

    private Status mStatus = Status.IDLE;

    private BroadcastReceiver mScreenCoverShowReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HSLog.d(TAG, "Cover show, start to clean");
            if (mStatus == STOPPING) {
                HSAccTaskManager.getInstance().cancel();
            } else if (mStatus != IDLE) {
                scanAndClean();
            }
        }
    };

    private BroadcastReceiver mSystemScreenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onScreenOff();
        }
    };
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                onPhoneCall();
            }
        }
    };

    private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onScreenOn();
        }
    };

    private final HSAppMemoryManager.MemoryTaskNoProgressListener mMemoryTaskNoProgressListener= new HSAppMemoryManager.MemoryTaskNoProgressListener() {
        @Override
        public void onSucceeded(List<HSAppMemory> list, long l) {
            onScanOver(list);

        }
        @Override
        public void onFailed(int i, String s) {
            HSLog.d(TAG, "Scan failed :" + s);
            onScanFail();
        }
    };

    private final CleanCallback mCleanCallback = new CleanCallback();

    public static void start(Context context) {
        Intent intent = new Intent(context, AutoCleanService.class);
        context.startService(intent);
    }

    private static boolean isServiceRunning(Context context, @NonNull Class clazz) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (clazz.getName().equals(info.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    public static void restart(Context context) {
        if (isServiceRunning(context, AutoCleanService.class)) {
            return;
        }
        Intent intent = new Intent(context, AutoCleanService.class);
        intent.setAction(ACTION_RESTART);
        context.startService(intent);
    }

    public static void stop(Context context) {
        if (!context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            HSLog.e(TAG, "can not stop from outside");
            return;
        }
        Intent intent = new Intent(context, AutoCleanService.class);
        intent.setAction(ACTION_STOP_NOW);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        sCleanService = this;
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mTelephoneManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        mLockHelper = new LockHelper(this);
        mLockHelper.reEnableKeyguard();

        mThirdPartyAllowList = (List<String>) HSConfig.getList("Application", "BoostPlus", "ThirdPartyAppsAllowList");
        mSystemAppsNeedClean = (List<String>) HSConfig.getList("Application", "BoostPlus", "SystemAppsKillList");

        HSAppMemoryManager.getInstance().setGlobalScanExcludeList(mThirdPartyAllowList);

        mInnerpermission = getString(R.string.receive_launch_broadcasts_permission);
        registerReceiver(mSystemScreenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(mScreenCoverShowReceiver, new IntentFilter(LockScreenCover.ACTION_BROADCAST_SHOW), mInnerpermission, null);
        TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        mSelfBootIntent = PendingIntent.getService(this, 0, new Intent(this, AutoCleanService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mCleanIntent = PendingIntent.getService(this, 1, new Intent(this, AutoCleanService.class).setAction(ACTION_CLEAN_NOW), PendingIntent.FLAG_UPDATE_CURRENT);
        scheduleNextBoot();
        improveLevelToForeground();

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        sCleanService = null;
        CommonUtils.unregisterReceiver(this, mSystemScreenOffReceiver);
        CommonUtils.unregisterReceiver(this, mScreenCoverShowReceiver);
        if (mPhoneStateListener != null) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            // restore clean task
        } else if (ACTION_STOP_NOW.equals(intent.getAction())) {
            HSLog.d(TAG, "Service stop");
            clearSchedule();
            stopSelf();
        } else if (ACTION_CLEAN_NOW.equals(intent.getAction())) {
            HSLog.d(TAG, "Service run now");
            doScheduleClean();
        }
        return START_STICKY;
    }

    private void onPhoneCall() {
        mInterrupted = true;
        clearSchedule();
        stopCleanTask();
    }

    private void clearSchedule() {
        if (mSelfBootIntent != null) {
            mAlarmManager.cancel(mSelfBootIntent);
        }

        if (mCleanIntent != null) {
            mAlarmManager.cancel(mCleanIntent);
        }
    }

    private void improveLevelToForeground() {
        Intent i = new Intent(this, InnerForegroundHelpService.class);
        startService(i);
    }

    private void onScreenOn() {
        HSLog.d(TAG, "Screen on");
        mAlarmManager.cancel(mCleanIntent);
        CommonUtils.unregisterReceiver(this, mScreenOnReceiver);
    }

    private void onScreenOff() {
        HSLog.d(TAG, "#Screen off#, CleanService status:" + mStatus.name());
        boolean reachCleanStandard = checkAndUpdateCleanStandard();

        if (mStatus != IDLE) {
            /**
             * User visit screen, so we stop cleaning job, and leave screen to user.
             *
             */
            mInterrupted = true;
            stopCleanTask();
            return;
        } else {
            mInterrupted = false;
        }

        HSLog.d(TAG, "#Start check user settings#");
        if (!isUserSettingsEnable() || !reachCleanStandard) {
            return;
        }

        if (System.nanoTime() - mLastCleanTimeNano < CLEAN_INTERVAL_MIN_TIME_NANO) {
            return;
        }

        // All conditions are ready, we schedule clean task or do immediately
        if (needScheduleClean()) {
            scheduleNextClean();
        } else {
            doClean(false);
        }
    }
    private void scheduleNextBoot() {
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME, AlarmManager.INTERVAL_HALF_DAY + SystemClock.elapsedRealtime(), mSelfBootIntent);
    }

    private void scheduleNextClean() {
        registerReceiver(mScreenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        mAlarmManager.cancel(mCleanIntent);
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + CLEAN_SCHEDULE_TIME_MILLS, mCleanIntent);
    }

    private boolean needScheduleClean() {
        boolean needSchedule = isInRootMood() || isEnvironmentAllowedSchedule();
        HSLog.d(TAG, "needSchedule:" + needSchedule);
        return needSchedule;
    }

    private void doScheduleClean() {
        HSLog.d(TAG, "do schedule clean");
        doClean(true);
    }

    void doClean(boolean schedule) {
        isCleanSchedule = schedule;
        if (isInRootMood()) {
            scanAndClean();
        } else if (isEnvironmentAllowed()) {
            mStatus = COVER_TO_SHOW;
            LockScreenCover.start(this);
        }
    }

    /**
     * check the interval time of screen off , screen off count
     * @return
     */
    private boolean checkAndUpdateCleanStandard() {
        long timeInterval = System.nanoTime() - mLastScreenOffTimeNano;
        mLastScreenOffTimeNano = System.nanoTime();
        int count = ++mScreenOffCount;
        int configCount = HSConfig.optInteger(8, "Application", "BoostPlus", "CleanIfScreenOffCount");
        long configInterval = HSConfig.optInteger(10, "Application", "BoostPlus", "CleanIfScreenOffIntervalTime") * 60000000000L;
        HSLog.d(TAG, "timeInterval:" + timeInterval + "-[C]" + configInterval + ";count:" + count + "-[C]" + configCount);
        if (count >= configCount || timeInterval >= configInterval ) {
            mScreenOffCount = 0;
            return true;
        }
        return false;
    }

    private boolean isUserSettingsEnable() {
        boolean currentState = HSPreferenceHelper.getDefault().getBoolean(BoostPlusSettingsActivity.PREF_KEY_AUTO_BOOST_ENABLED, false);
        HSLog.d(TAG, "auto-clean enable : " + currentState);
        return currentState;
    }

    private void onScanFail() {
        setStatus(IDLE);
    }

    private void onScanOver(List<HSAppMemory> list) {
        HSLog.d(TAG, "Scan app total:" + list);
        List<String> cleanList = new ArrayList<>();
        for (HSAppMemory appMemory : list) {
            if (isAppInKillList(appMemory)) {
                cleanList.add(appMemory.getPackageName());
            }
        }

        if (mStatus == SCANNING) {
            HSLog.d(TAG, "Scan app ,need clean:" + cleanList);
            if (isInRootMood()) {
                launchCleanerRootMode(cleanList);
            } else {
                boolean cleaning = launchCleaner(cleanList);
                if (!cleaning) {
                    setStatus(IDLE);
                }
            }
        } else {
            setStatus(IDLE);
            HSLog.e(TAG, "Expect Scanning state, but result is:" + mStatus.name());
        }
    }

    public void setStatus(Status status) {
        if (status == IDLE) {
            if (mStatus.ordinal() > COVER_TO_SHOW.ordinal()) {
                release();
            }
            // User intent to wake up screen.
            if (!mInterrupted) {
                AdminReceiver.lockNow(this);
            }
        }
        mStatus = status;
    }

    private void scanAndClean() {
        HSLog.d(TAG, "======Start Scan Apps=====");

        mStatus = SCANNING;
        HSAppMemoryManager.getInstance().startScanWithoutProgress(mMemoryTaskNoProgressListener);
    }

    private void launchCleanerRootMode(List<String> pendingCleanList) {
        mStatus = ROOT_CLEAN;
        List<HSAppMemory> memoryList = new ArrayList<>();
        for (String packageName : pendingCleanList) {
            memoryList.add(new HSAppMemory(packageName));
        }

        HSAppMemoryManager.getInstance().startClean(memoryList, false, mCleanCallback);
    }

    private boolean launchCleaner(List<String> pendingCleanList) {
        if (pendingCleanList.isEmpty()) {
            onCleanOver();
            return false;
        }

        if (!LockHelper.isKeyguardAllowToAutoClean(this)) {
            HSLog.e(TAG, "Keyguard is lock!!");
            return false;
        }

        // Do clean work.
        if (mStatus == SCANNING) {
            mStatus = NORMAL_CLEAN;
        }
        mLockHelper.disableKeyguard();
        removeHasStoppedApps(pendingCleanList);
        HSAccTaskManager.getInstance().startForceStop(pendingCleanList, mCleanCallback);
        return true;
    }

    private void removeHasStoppedApps(List<String> pendingCleanList) {
        List<String> newList = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        for (String packageName : pendingCleanList) {
            try {
                ApplicationInfo packInfo = packageManager.getApplicationInfo(packageName, 0);
                boolean stopped = (packInfo.flags & ApplicationInfo.FLAG_STOPPED) != 0;
                boolean inActive = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    inActive = ((UsageStatsManager)getApplicationContext().getSystemService(USAGE_STATS_SERVICE)).isAppInactive(packageName);
                }
                if (!stopped && !inActive) {
                    newList.add(packageName);
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        pendingCleanList.clear();
        pendingCleanList.addAll(newList);
    }

    private void stopCleanTask() {
        if (mStatus == NORMAL_CLEAN) {
            HSLog.d(TAG, "Clean cancelling");
            mStatus = STOPPING;
        } else if (mStatus == SCANNING) {
            HSLog.d(TAG, "Scan cancelling");
            setStatus(IDLE);
        }
        HSAppMemoryManager.getInstance().stopScan(mMemoryTaskNoProgressListener);
        HSAccTaskManager.getInstance().cancel();
        HSAnalytics.logEvent("BoostPlus_AutoClean_Work", "Type", "Interrupted_" + (isCleanSchedule ? "3min" : "2.5s"));
    }

    private void onCleanFailed() {
        mLockHelper.reEnableKeyguard();

        if (mStatus == STOPPING) {
            mScreenOffCount--;
        }
       setStatus(IDLE);
    }

    private void onCleanOver() {
        HSAnalytics.logEvent("BoostPlus_AutoClean_Work", "Type", "Success_" + (isCleanSchedule ? "3min" : "2.5s"));
        mLockHelper.reEnableKeyguard();
        mLastCleanTimeNano = System.nanoTime();

        // Lock screen
        if (mStatus == NORMAL_CLEAN) {
            mScreenOffCount--;
        }
        setStatus(IDLE);
    }

    private void release() {
        LockScreenCover.stop(AutoCleanService.this);
    }

    // TODO facebook not be kill!!!
    private boolean isAppInKillList(HSAppMemory appMemory) {
        if (!appMemory.isSysApp()) {
            return true;
        }
        if (mSystemAppsNeedClean == null) {
            return false;
        }

        for (String name : mSystemAppsNeedClean) {
            return name.equals(appMemory.getPackageName());
        }

        return false;
    }


    private boolean isInRootMood() {
        // We don't call RootHelper.grantRootPermissionWithTimeout(). Instead we get last result quietly.
        return RootHelper.getLastPermissionGrantResult();
    }

    private boolean isEnvironmentAllowed() {
       return LockHelper.isKeyguardAllowToAutoClean(this)
               && AdminReceiver.isActiveAdmin(this)
               && PermissionUtils.isAccessibilityGranted(this)
               && CommonUtils.isFloatWindowAllowed(this)
               && !hasPhoneCall();
    }

    private boolean hasPhoneCall() {
        // If has phone call, wo not do clean job.
        return (mTelephoneManager.getCallState() != TelephonyManager.CALL_STATE_IDLE);
    }

    private boolean isEnvironmentAllowedSchedule() {
        return !LockHelper.getKeyguardService(this).isKeyguardSecure() &&
                AdminReceiver.isActiveAdmin(this) &&
                PermissionUtils.isAccessibilityGranted(this);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Helper service to improve service level
     */
    public static class InnerForegroundHelpService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (sCleanService != null && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                sCleanService.startForeground(1, new Notification());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    startForeground(1, new Notification());
                }
                HSLog.d(TAG, "Service improve to 'Foreground level'");
            }
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
    }

    private class CleanCallback implements HSAccTaskManager.AccTaskListener,HSAppMemoryManager.MemoryTaskListener {

        @Override
        public void onStarted() {
            HSLog.d(TAG, "Clean start, Mode:" + mStatus);
        }

        @Override
        public void onProgressUpdated(int i, int i1, HSAppMemory hsAppMemory) {
            HSLog.d(TAG, "Clean " + i + "/" + i1 + " : " + hsAppMemory);
        }

        @Override
        public void onProgressUpdated(int i, int i1, String s) {
            HSLog.d(TAG, "Clean " + i + "/" + i1 + " : " + s);
        }

        @Override
        public void onSucceeded() {
            onCleanOver();
        }

        @Override
        public void onSucceeded(List<HSAppMemory> list, long l) {
            onCleanOver();
        }

        @Override
        public void onFailed(int i, String s) {
            HSLog.d(TAG, "Clean fail, reason is " + s);
            onCleanFailed();
        }
    }
}

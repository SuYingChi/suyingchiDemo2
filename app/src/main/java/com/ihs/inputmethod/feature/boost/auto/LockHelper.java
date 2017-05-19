package com.ihs.inputmethod.feature.boost.auto;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Process;
import android.provider.Settings;

import com.ihs.commons.utils.HSLog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper to Detect keyguard state
 */

public class LockHelper {

    public static final int ALLOW_AUTO_CLEAN = 1;
    public static final int LOCK_NO_DELAY = -1;
    public static final int LOCK_INSTANTLY = -2;

    private final Context mAppContext;
    private final KeyguardManager mKeyguardManager;
    private final KeyguardManager.KeyguardLock mKeyguardLocker;

    private final static List<Object> sTobeCancelTasks = new ArrayList<>();

    public LockHelper(Context context) {
        mAppContext = context.getApplicationContext();
        mKeyguardManager = (KeyguardManager) mAppContext.getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLocker = mKeyguardManager.newKeyguardLock("locker");
    }

    public void disableKeyguard() {
        mKeyguardLocker.disableKeyguard();
    }

    public void reEnableKeyguard() {
        mKeyguardLocker.reenableKeyguard();
    }

    /**
     * The value of 'Security -> Auto lock' in Settings.
     * @param context
     * @return time mills that
     */
    public static long getSecureSettingForAutoLockDelayTime(Context context) {
        return Settings.Secure.getLong(context.getContentResolver(), "lock_screen_lock_after_timeout", -1);
    }

    /**
     * 1  DeviceAdmin access
     * 2  AccessiblityService access
     * 3  LockScreen settings
     * @param context
     * @return
     */
    public static boolean isAllowToAutoClean(Context context){

        return false;
    }

    /**
     * If current settings allow auto clean
     * @param context
     * @return
     */
    public static boolean isKeyguardAllowToAutoClean(Context context) {
        int state = getKeyguardState(context);
        HSLog.i("LockerHelper", "lockKeyguard state:" + state);
        return state == ALLOW_AUTO_CLEAN;
    }

    public static int getKeyguardState(Context context) {
        if (!getKeyguardService(context).isKeyguardSecure()) {
            return ALLOW_AUTO_CLEAN;
        }

        if (getSecureSettingForAutoLockDelayTime(context) < 5000) {
            return LOCK_NO_DELAY;
        }

        if (isInstantlyLockEnable(context)) {
            return LOCK_INSTANTLY;
        }

        return ALLOW_AUTO_CLEAN;
    }

    public static KeyguardManager getKeyguardService(Context context) {
        return (KeyguardManager) context.getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
    }

    public static ContentObserver startObservingLockDelayTime( Context c, final Runnable runnable) {
        final Context context = c.getApplicationContext();
        Handler watchdog = new Handler();
        final ContentObserver observer = new ContentObserver(new Handler()) {
            @SuppressLint("InflateParams")
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (getSecureSettingForAutoLockDelayTime(context) >= 5000) {
                    if (runnable != null) {
                        runnable.run();
                    }
                    context.getContentResolver().unregisterContentObserver(this);
                }
            }
        };
        watchdog.postDelayed(new Runnable() {
            @Override
            public void run() {
                context.getContentResolver().unregisterContentObserver(observer);
                sTobeCancelTasks.remove(observer);
            }
        }, 30 * 1000);
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_lock_after_timeout"), false, observer);
        sTobeCancelTasks.add(observer);
        return observer;
    }

    public static CountDownTimer startObservingLockInstantly( Context c, final Runnable runnable) {
        final Context context = c.getApplicationContext();
        CountDownTimer countDownTimer = new CountDownTimer(1000 * 30, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                boolean ok = !isInstantlyLockEnable(context);
                if (ok) {
                    this.cancel();
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            }

            @Override
            public void onFinish() {
            }
        };

        countDownTimer.start();
        sTobeCancelTasks.add(countDownTimer);
        return countDownTimer;
    }

    public static void cancelAllTasksAndCallbacks(Context c) {
        Context context = c.getApplicationContext();
        for (Object o : sTobeCancelTasks) {
            if (o instanceof ContentObserver) {
                context.getContentResolver().unregisterContentObserver((ContentObserver) o);
            } else if (o instanceof CountDownTimer) {
                ((CountDownTimer) o).cancel();
            }
        }
        sTobeCancelTasks.clear();
    }

    /**
     * Check value of settings :'Secure -> Power Button Instantly locks'
     * @param context
     * @return
     */
    public static boolean isInstantlyLockEnable(Context context) {
        final String clazzName = "com.android.internal.widget.LockPatternUtils";
        final String methodName = "getPowerButtonInstantlyLocks";
        try {
            Class cls = Class.forName(clazzName);
            Class parType = Context.class;
            Object utilInstance = cls.getConstructor(parType).newInstance(context.getApplicationContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Method method = cls.getDeclaredMethod(methodName, int.class);
                return (boolean) method.invoke(utilInstance, Process.myUserHandle().hashCode());
            } else {
                Method method = cls.getDeclaredMethod(methodName);
                return (boolean) method.invoke(utilInstance);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

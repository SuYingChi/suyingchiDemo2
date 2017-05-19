package com.ihs.inputmethod.feature.boost.plus;

import android.os.SystemClock;

import com.honeycomb.launcher.model.LauncherFiles;
import com.honeycomb.launcher.util.ConcurrentUtils;
import com.honeycomb.launcher.util.PreferenceHelper;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.memory.utils.HSRootUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RootHelper {

    private static final String TAG = RootHelper.class.getSimpleName();

    private static final String PREF_KEY_LAST_ROOT_RESULT = "last_root_result";
    private static final String PREF_KEY_CONTINUOUS_TIMEOUT_COUNT = "continuous_timeout_count";

    private static final int FLAG_PERMISSION_GRANTED = 1;

    private static final int FLAG_USER_ACTION_INVOLVED = 1 << 1;

    private static final int FLAG_TIMEOUT = 1 << 2;

    private static final int DEFAULT_PERMISSION_REQUEST_TIMEOUT_SECONDS = 10;

    /**
     * If time out twice in a row, we never try again.
     */
    private static final int PERMANENT_FAILURE_CONTINUOUS_TIMEOUT_COUNT = 2;

    /**
     * When a call to {@link HSRootUtils#grantRootPermission()} blocks calling thread for more than 1000 ms, it is
     * inferred that user action has been involved (through a dialog). We do not ask for accessibility permission
     * even if we didn't get root permission in this case.
     */
    private static final long ROOT_PERMISSION_USER_ACTION_INVOLVED_TIME_THRESHOLD = 1000;

    private static final long sPermissionRequestTimeout;
    static {
        sPermissionRequestTimeout = 1000 * HSConfig.optInteger(DEFAULT_PERMISSION_REQUEST_TIMEOUT_SECONDS,
                "Application", "BoostPlus", "RootPermissionTimeoutSeconds");
    }

    static int grantRootPermissionWithTimeout() {
        PreferenceHelper prefs = PreferenceHelper.get(LauncherFiles.BOOST_PREFS);
        if (prefs.getInt(PREF_KEY_CONTINUOUS_TIMEOUT_COUNT, 0) > PERMANENT_FAILURE_CONTINUOUS_TIMEOUT_COUNT) {
            HSLog.i(TAG, "Root permission request timed out twice, never try grant permission again");
            return 0;
        }
        int result = 0;
        long before = SystemClock.elapsedRealtime();
        try {
             if ((boolean) ConcurrentUtils.callWithTimeout(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return HSRootUtils.grantRootPermission();
                }
             }, sPermissionRequestTimeout, TimeUnit.MILLISECONDS)) {
                 result |= FLAG_PERMISSION_GRANTED;
             }
        } catch (TimeoutException e) {
            HSLog.w(TAG, "Root permission request timed out");
            result |= FLAG_TIMEOUT;
        }
        long grantDuration = SystemClock.elapsedRealtime() - before;
        boolean granted = ((result & FLAG_PERMISSION_GRANTED) != 0);
        boolean timedOut = ((result & FLAG_TIMEOUT) != 0);
        HSLog.d(TAG, "It took " + grantDuration + " ms to grant root permission, success: "
                + granted + ", timeout " + timedOut);
        if (grantDuration > ROOT_PERMISSION_USER_ACTION_INVOLVED_TIME_THRESHOLD) {
            result |= FLAG_USER_ACTION_INVOLVED;
        }
        if (timedOut) {
            prefs.incrementAndGetInt(PREF_KEY_CONTINUOUS_TIMEOUT_COUNT);
        } else {
            prefs.putInt(PREF_KEY_CONTINUOUS_TIMEOUT_COUNT, 0);
        }
        prefs.putBoolean(PREF_KEY_LAST_ROOT_RESULT, granted);
        return result;
    }

    static boolean isPermissionGranted(int grantResult) {
        return (grantResult & FLAG_PERMISSION_GRANTED) != 0;
    }

    static boolean isUserActionInvolved(int grantResult) {
        return (grantResult & FLAG_USER_ACTION_INVOLVED) != 0;
    }

    public static boolean getLastPermissionGrantResult() {
        return PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getBoolean(PREF_KEY_LAST_ROOT_RESULT, false);
    }
}

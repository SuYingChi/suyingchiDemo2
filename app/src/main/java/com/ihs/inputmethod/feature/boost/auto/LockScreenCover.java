package com.ihs.inputmethod.feature.boost.auto;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.honeycomb.launcher.BuildConfig;
import com.honeycomb.launcher.R;
import com.honeycomb.launcher.util.CommonUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.devicemonitor.accessibility.HSAccessibilityService;

import java.lang.reflect.Field;

/**
 * Cover screen after screen off, we do auto clean below that cower, it looks like nothing happen.
 */

public class LockScreenCover extends Service{

    private static final String TAG = LockScreenCover.class.getSimpleName();
    public static final String ACTION_BROADCAST_SHOW = BuildConfig.APPLICATION_ID + ".action.LOCK_COVER_SHOW";
    private static final String ACTION_CLEAR = "CLEAR";
    private static final String ACTION_KEEP_SHOW = "KEEP_SHOW";
    private static final long TIME_OUT_SELF_QUIT = 600000;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private IBinder accessibilityServiceToken;

    private TextView textView;

    private final BroadcastReceiver mStopSelfReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                CommonUtils.unregisterReceiver(context, this);
                HSLog.d(TAG, "Receive stop self command");
                LockScreenCover.this.stopSelf();
            }
        }
    };

    /**
     * This receiver just for emergency status. if not stop by {@link AutoCleanService}, we stop self after sceen on third times.
     */
    private final BroadcastReceiver mScreenOnCounter = new BroadcastReceiver() {
        private int mScreenOnCount;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                mScreenOnCount++;
                if (mScreenOnCount >= 3) {
                    LockScreenCover.this.stopSelf();
                    mScreenOnCount = 0;
                }
            }
        }
    };

    private Runnable mTimeOutWorker =  new Runnable() {
        @Override
        public void run() {
            stopSelf();
        }
    };

    public static void start(Context context) {
        Intent i = new Intent(context, LockScreenCover.class);
        context.startService(i);
    }

    public static void stop(Context context) {
        Intent i = new Intent(context, LockScreenCover.class);
        i.setAction(ACTION_CLEAR);
        context.startService(i);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        int flag = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES;

        int flag2 = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        accessibilityServiceToken = getAccessibilityServiceToken();
        HSLog.d(TAG, "accessibilityToken:" + accessibilityServiceToken);
        int type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && accessibilityServiceToken != null) {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        }

        mLayoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                type, flag2 | flag, PixelFormat.TRANSPARENT);
    }

    @Override
    public void onDestroy() {
        try {
            clearContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        CommonUtils.unregisterReceiver(this, mScreenOnCounter);
        CommonUtils.unregisterReceiver(this, mStopSelfReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Show Dimmer
        if (intent == null) {
            // Restart by system.
        } else if (ACTION_CLEAR.equals(intent.getAction())) {
            HSLog.d(TAG, "Cover going to dismiss");
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        try {
            if (textView != null) {
                mWindowManager.removeView(textView);
                textView.removeCallbacks(mTimeOutWorker);
            }
        } catch (Exception e) {
            // Failed, no do work
            return START_NOT_STICKY;
        }

        mLayoutParams.dimAmount = BuildConfig.DEBUG ? 0.3f : 1.0f;
        mLayoutParams.screenBrightness = 0.01f;
        mLayoutParams.buttonBrightness = 0;
        mLayoutParams.width = CommonUtils.getPhoneWidth(this);
        mLayoutParams.height = (int) (CommonUtils.getPhoneHeight(this) * 1.2);
        mLayoutParams.gravity = Gravity.CENTER;
        mLayoutParams.token = accessibilityServiceToken;

        textView = new TextView(this);
        textView.setTextColor(Color.YELLOW);
        textView.setGravity(Gravity.CENTER);
        mWindowManager.addView(textView, mLayoutParams);

        textView.postDelayed(mTimeOutWorker, TIME_OUT_SELF_QUIT);
        textView.post(new Runnable() {
            @Override
            public void run() {
                onCoverShown();
            }
        });

        return START_NOT_STICKY;
    }

    private void onCoverShown() {
        HSLog.d(TAG, "Cover view shown");
        registerReceiver(mStopSelfReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(mScreenOnCounter, new IntentFilter(Intent.ACTION_SCREEN_ON));

        String permission = getString(R.string.receive_launch_broadcasts_permission);
        sendBroadcast(new Intent(ACTION_BROADCAST_SHOW), permission);
    }

    private IBinder getAccessibilityServiceToken() {
        AccessibilityService service = HSAccessibilityService.getInstance();
        try {
            Field field = AccessibilityService.class.getDeclaredField("mWindowToken");
            if (!field.isAccessible()) {
                field.setAccessible(true);
                return (IBinder) field.get(service);
            }
        } catch (Exception e) {
            HSLog.e(e.getMessage());
        }
        return null;
    }

    private void clearContent() {
        if (textView != null) {
            textView.removeCallbacks(mTimeOutWorker);
            mWindowManager.removeView(textView);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

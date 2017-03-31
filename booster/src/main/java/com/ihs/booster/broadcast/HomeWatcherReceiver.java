package com.ihs.booster.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ihs.app.framework.HSApplication;
import com.ihs.booster.common.event.MBObserverEvent;
import com.ihs.booster.manager.MBBoostManager;
import com.ihs.booster.utils.L;

/**
 * Created by zhixiangxiao on 3/2/16.
 */
@Deprecated
public class HomeWatcherReceiver extends BroadcastReceiver {
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
    private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";
    private static HomeWatcherReceiver mHomeKeyReceiver;

    public static void registerHomeKeyReceiver() {
        try {
            mHomeKeyReceiver = new HomeWatcherReceiver();
            final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            HSApplication.getContext().registerReceiver(mHomeKeyReceiver, homeFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unregisterHomeKeyReceiver() {
        try {
            if (null != mHomeKeyReceiver) {
                HSApplication.getContext().unregisterReceiver(mHomeKeyReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            L.l("SYSTEM_DIALOG_REASON:" + reason + " action:" + action);
            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                MBBoostManager.globalObserverCenter.notifyOnUIThread(MBObserverEvent.SYSTEM_HOME_KEY_PRESSED_SHORT);
                // 短按Home键
            } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                // 长按Home键 或者 activity切换键

            } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                // 锁屏
            } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                // samsung 长按Home键
            }
        }
    }
}

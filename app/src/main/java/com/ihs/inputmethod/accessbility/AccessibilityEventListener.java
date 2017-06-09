package com.ihs.inputmethod.accessbility;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.devicemonitor.accessibility.IAccEventListener;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.utils.Constants;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
import static com.ihs.inputmethod.accessbility.AccGALogger.app_permission_accessibility_allowed;
import static com.ihs.inputmethod.accessbility.AccGALogger.logOneTimeGA;

/**
 * Created by Arthur on 16/12/28.
 */

public class AccessibilityEventListener implements IAccEventListener {

    public static final int MODE_SETUP_KEYBOARD = 1;

    private int modeCode = -1;

    public AccessibilityEventListener(int code) {
        modeCode = code;
    }

    private SetupKeyboardAccessibilityService accessibilityService;

    @Override
    public IBinder asBinder() {
        return null;
    }

    @Override
    public void onAvailable() throws RemoteException {

        HSLog.e("accessbility enabled" + this.toString());
        logOneTimeGA(app_permission_accessibility_allowed);

        switch (modeCode) {
            case MODE_SETUP_KEYBOARD:
                if (!HSInputMethodListManager.isMyInputMethodSelected()) {
                    boolean inputMethodEnabled = HSInputMethodListManager.isMyInputMethodEnabled();
                    if (accessibilityService == null) {
                        accessibilityService = new SetupKeyboardAccessibilityService();
                    }
                    accessibilityService.onServiceConnected();

                    if(inputMethodEnabled){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager m = (InputMethodManager) HSApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                m.showInputMethodPicker();
                            }
                        },100);
                    }else{
                        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                        intent.addFlags(FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | FLAG_ACTIVITY_REORDER_TO_FRONT
                                | FLAG_ACTIVITY_NEW_TASK
                                | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP
                                | FLAG_ACTIVITY_NO_HISTORY);
                        HSApplication.getContext().startActivity(intent);
                    }
                }
                break;
        }

    }

    @Override
    public void onEvent(AccessibilityEvent accessibilityEvent) throws RemoteException {
        switch (modeCode) {
            case MODE_SETUP_KEYBOARD:
                if (!HSInputMethodListManager.isMyInputMethodSelected()) {
                    setupKeyboard(accessibilityEvent);
                }
                break;
        }
    }


    @Override
    public void onUnavailable(int i, String s) throws RemoteException {
    }


    private void setupKeyboard(AccessibilityEvent accessibilityEvent) {
        if (accessibilityService == null) {
            accessibilityService = new SetupKeyboardAccessibilityService();
        }
        accessibilityService.onAccessibilityEvent(accessibilityEvent);
    }

    public void onDestroy() {
        if (accessibilityService != null) {
            accessibilityService.onDestroy();
        }
    }
}

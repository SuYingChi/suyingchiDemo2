package com.mobipioneer.lockerkeyboard.accessbility;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.devicemonitor.accessibility.IAccEventListener;
import com.ihs.inputmethod.api.framework.HSInputMethod;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;
import static com.mobipioneer.lockerkeyboard.accessbility.AccGALogger.app_permission_accessibility_allowed;
import static com.mobipioneer.lockerkeyboard.accessbility.AccGALogger.logOneTimeGA;

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
                if (!HSInputMethod.isCurrentIMESelected()) {
                    if (!HSInputMethod.isCurrentIMEEnabled()) {
                        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_NO_HISTORY);
                        HSApplication.getContext().startActivity(intent);
                    }
                    if (accessibilityService == null) {
                        accessibilityService = new SetupKeyboardAccessibilityService();
                    }
                    accessibilityService.onServiceConnected();
                }
                break;
        }

    }

    @Override
    public void onEvent(AccessibilityEvent accessibilityEvent) throws RemoteException {
        switch (modeCode) {
            case MODE_SETUP_KEYBOARD:
                if (!HSInputMethod.isCurrentIMESelected()) {
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

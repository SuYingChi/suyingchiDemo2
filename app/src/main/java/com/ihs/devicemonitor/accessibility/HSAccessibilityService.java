package com.ihs.devicemonitor.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;

import com.ihs.inputmethod.accessbility.AccessibilityEventListener;

/**
 * Created by Arthur on 2018/1/31.
 */

public class HSAccessibilityService {
    private static HSAccessibilityService instance;

    public static HSAccessibilityService getInstance() {
        return instance =  new HSAccessibilityService();
    }

    public static boolean isAvailable() {
        return false;
    }

    public AccessibilityNodeInfo getRootInActiveWindow() {
        return null;
    }

    public static int registerEventListener(AccessibilityEventListener accessibilityEventListener) {
        return 0;
    }

    public static void unregisterEvent(int listenerKey) {

    }
}

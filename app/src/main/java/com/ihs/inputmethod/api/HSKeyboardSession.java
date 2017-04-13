package com.ihs.inputmethod.api;

import android.preference.PreferenceManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.framework.HSInputMethod;

/**
 * Created by ihandysoft on 17/4/12.
 */

public class HSKeyboardSession {

    public static final String NOTIFICATION_KEYBOARD_SHOW_INPUTMETHOD = "notification_keyboard_session_show_inputmethod";
    public static final String SP_KEYBOARD_SESSION_CURRENT_INDEX = "keyboard_session_current_index";


    public static HSKeyboardSession keyboardSession;

    private HSKeyboardSession() {
        INotificationObserver observer = new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                if(HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD.equals(s)) {
                    int sessionIndex = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getInt(SP_KEYBOARD_SESSION_CURRENT_INDEX, -1);
                    PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putInt(SP_KEYBOARD_SESSION_CURRENT_INDEX, sessionIndex + 1).apply();
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_KEYBOARD_SHOW_INPUTMETHOD);
                }
            }
        };
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD, observer);
    }

    public static HSKeyboardSession getInstance() {
        if(keyboardSession == null) {
            synchronized (HSKeyboardSession.class) {
                if(keyboardSession == null) {
                    keyboardSession = new HSKeyboardSession();
                }
            }
        }
        return keyboardSession;
    }

    public int getKeyboardSessionIndex() {
        return PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getInt(SP_KEYBOARD_SESSION_CURRENT_INDEX, -1);
    }
}

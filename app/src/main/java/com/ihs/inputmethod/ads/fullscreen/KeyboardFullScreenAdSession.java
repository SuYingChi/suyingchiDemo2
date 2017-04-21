package com.ihs.inputmethod.ads.fullscreen;

import android.preference.PreferenceManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.framework.HSInputMethod;

/**
 * Created by ihandysoft on 17/4/12.
 */

public class KeyboardFullScreenAdSession {

    static final String NOTIFICATION_KEYBOARD_SHOW_INPUTMETHOD = "notification_keyboard_session_show_inputmethod";
    private static final String SP_KEYBOARD_SESSION_CURRENT_INDEX = "keyboard_session_current_index";


    private static KeyboardFullScreenAdSession keyboardSession;

    private KeyboardFullScreenAdSession() {
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

    public static KeyboardFullScreenAdSession getInstance() {
        if(keyboardSession == null) {
            synchronized (KeyboardFullScreenAdSession.class) {
                if(keyboardSession == null) {
                    keyboardSession = new KeyboardFullScreenAdSession();
                }
            }
        }
        return keyboardSession;
    }

    static int getKeyboardFullScreenAdSessionIndex() {
        return PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getInt(SP_KEYBOARD_SESSION_CURRENT_INDEX, -1);
    }

    public static void resetKeyboardFullScreenAdSessionIndex() {
        PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putInt(KeyboardFullScreenAdSession.SP_KEYBOARD_SESSION_CURRENT_INDEX, 0).apply();
    }
}

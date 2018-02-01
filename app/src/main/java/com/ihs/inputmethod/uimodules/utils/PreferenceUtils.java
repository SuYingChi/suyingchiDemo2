package com.ihs.inputmethod.uimodules.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ihs.app.framework.HSApplication;

/**
 * Created by jixiang on 16/4/20.
 */
public class PreferenceUtils {
    private final static String SP_NAME = "emojikeyboard";

    public static boolean getBoolean(String key) {
        SharedPreferences sp = HSApplication.getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static boolean getDefaultSharedPreferencesBoolean(String key, boolean defaultValue) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
//        return sp.getBoolean(key, defaultValue);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public static void setBoolean(String key, boolean value) {
        SharedPreferences sp = HSApplication.getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }
}
